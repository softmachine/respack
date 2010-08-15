package at.softmachine.grails.plugins

class ResourceTagLib {
  def groovyPagesTemplateEngine
  static namespace = "respack"

  /**
   * adds a resource of some type to a packlist to be rendered later
   */
  def addresource = {attrs, body ->
    def file = attrs.file
    def type  = attrs.type ?:  "css"
    def packid   = attrs.packid ?: "${type}-id"
    def parse = attrs.boolean ("parse")

    def idlist = openPack(session, packid)
    idlist << [type:type, file:file, parse:parse]
  }

  /**
   * adds a css resource to a packlist
   */
  def addcss = {attrs, body ->
    out << addresource (attrs+[type:'css'])
    out << "<!-- addcss: $attrs.file included-->"
  }


   /**
    * add script resource to a packlist
    */
  def addscript = {attrs, body ->
    out << addresource (attrs+[type:'js'])
    out << "<!-- addscript: $attrs.file included -->"
  }




  /**
  * renders a packlist according to the supplied parameters
   */
  def renderpack = {attrs,body->
    def type  = attrs.remove('type') ?:  "css"
    def packid   = attrs.remove('packid') ?: "${type}-id"
    def baseDir = attrs.remove('basedir') ?: ""
    def mode = attrs.remove('mode') ?: "links"

    switch (type) {
      case 'css':
        out << render_css (packid, baseDir, mode, attrs)
        break ;
      case 'script':
        out << render_script (packid, baseDir, mode, attrs)
        break ;
      case 'plain':
        render_plain (out, packid, baseDir, mode, attrs)
        break ;

      default:
        out << "cannot render resource pack: unknown type: $type"
    }
  }

  private String render_css (packid, baseDir, mode, attrs) {
    List idlist = openPack(session, packid)

    StringBuilder sb = new StringBuilder()

    switch (mode) {
      case 'links':
        idlist.each {
          sb << '<!-- render: --><link rel=\"stylesheet" ' << 'href="'<< g.resource(dir:baseDir, file:it.file) <<'">\n'
        }
        break ;

      case 'inline':
        sb << tag (name:'style', type:'text/css', media:'all') {
          idlist.inject (new StringBuilder()) {sbi, item->
            def fpath = "$baseDir/$item.file"
            sbi << include (path:fpath, parse:item.parse) ;
          }
        }
        break ;

      case 'onelink':
        def respath = g.resource(dir:baseDir, file:packid+".gss").toString()
        sb << respack.tag (name:'link', rel:'stylesheet', href:respath).toString()
        return sb // idlist remains valid here for the resourcecontroller to pick it up later

      default:
        out << "<!-- render: cannot render css resource pack $packid: unknown mode: $mode -->\n"
      }

    idlist.clear()
    closePack (session, packid)

    return sb.toString()
  }

  private String  render_script (packid, baseDir, mode, attrs) {
    List idlist = openPack(session, packid)

    StringBuilder sb = new StringBuilder() ;

    switch (mode) {
      case 'links':
        idlist.each {
          sb << "<!-- render: -->" << respack.tag (name:'script', type:'text/javascript', src:g.resource(dir:baseDir, file:it.file)) << "\n"
        }
        break ;

      case 'inline':
        sb << respack.tag (name:'script', type:'text/javascript') {
          idlist.inject (new StringBuilder()) {sbi, item->
            def fpath = "$baseDir/$item.file"
            sbi << include (path:fpath, parse:item.parse) ;
          }
        }
        break ;

      case 'onelink':
        sb << respack.tag (name:'script', type:'text/javascript', src:g.resource(dir:baseDir, file:"${packid}.gjs"))
        return sb.toString() // makes sure that idlist remains valid for the resourcecontroller to pick up later

      default:
        sb << "<!-- render: cannot render css resource pack $packid: unknown mode: $mode -->\n"
    }

    closePack(session, packid)
    return sb.toString()
  }


  def rendercss = {attrs, body ->
    def id   = attrs.remove ('id') ?: 'cssid'
    def baseDir = attrs.remove('basedir') ?: ""
    def mode = attrs.remove('mode') ?: "links"

    out << "\n"

    List idlist = openPack(session, id)
    switch (mode) {
      case 'links':
        idlist.each {
          out << '<!-- render: --><link rel=\"stylesheet" ' << 'href="'<< g.resource(dir:baseDir, file:it.file) <<'">\n'
        }
        break ;
      case 'inline':
        out << tag (name:'style', type:'text/css', media:'all') {
          idlist.each {
            def fpath = "$baseDir/$it.file"
            out << include (path:fpath, parse:true) ;
          }
        }
        break ;

      case 'onelink':
        out << tag (name:'link', rel:'stylesheet', href:g.resource(dir:baseDir, file:"${id}.gss"))
        return // make sure that idlist remains valid for the resourcecontroller to pick up later
        break ;

      default:
        out << "<!-- oops ! render mode '$mode' is not supported for id '$id' -->"

    }
    idlist.clear() ;
  }

  def include = {attrs, body ->
    def path = attrs.remove ('path')?.toString()
    def parse = new Boolean(attrs.remove('parse') ?: "false")

    log.debug ("processing resource with path: $path (parse=$parse)")
//    out << "<!-- resource: $path (parse=$parse) -->\n"

    def fullPath = request?.session?.servletContext?.getRealPath(path)
    File resFile = new File(fullPath) ;

    if (!resFile.canRead()) {
//      out << "   <!-- error: file not found: $fullpath-->"
      return ;
    }

    if (parse) {
      def template = groovyPagesTemplateEngine.createTemplate(resFile)
      Writable result = template.make([params:params, config:grailsApplication.config])
      result.writeTo(out)
    }
    else {
      out << resFile.text
    }

  }

  def tag = {attrs, body->
    def name = attrs.remove('name')

    def attrstring = attrs.collect {key,val-> " $key=\"${val.toString()}\""}?.join(" ")

    if (!body)
      out << "<$name $attrstring />"
    else {
      out << "<$name $attrstring >"
      out << body()
      out << "</$name>"
    }
  }


  private static List openPack (session, id) {
    if (!session['owfres.respack'])
      session['owfres.respack'] = [:]

    def idlist = session['owfres.respack'][id]
    if (!idlist) {
      idlist = []
      session['owfres.respack'][id] = idlist
    }

    return idlist
  }

  private static void closePack (session, id) {
    if (session['owfres.respack']) {
      if (session['owfres.respack'][id])
        session['owfres.respack'].remove(id)

      if (session['owfres.respack'].size() == 0)
        session['owfres.respack'] = null ;
    }
  }

}
