package at.softmachine.grails.plugins.respack
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.apache.commons.httpclient.util.DateUtil
import at.softmachine.grails.plugins.ResourceTagLib

/**
 * enhances handling of (.js and .css) resources in some ways
 *
 * 1.) makes it possible to render .css, .js (and whatever else) as GSP. which makes it possible to access
 * grails (request) vars inside .js and .css and to apply (conditional) groovy code as well
 *
 * 2.) makes it possible to join multiple resources of the same type into one (large) to reduce the number of
 * request the browser needs to make to reduce application latency.
 *
 * 3.) provides a way to compress .css and .js resources to further reduce download size and latency
 *
 */

class ResourceController {
  GroovyPagesTemplateEngine groovyPagesTemplateEngine


  def renderAsGsp = {
    if (!params.path) {
      response.sendError("500", "missing mandatory parameter: 'path'")
      return
    }

    params.encode = params.encode ?: "UTF-8"
    params.contentType = params.contentType ?: contentTypeFromPath(params.path)
    log.debug ("render as GSP: $params.path from $params.dir with contentType $params.contentType encoded as $params.encode")

    def fullPath = request?.session?.servletContext?.getRealPath("$params.dir/$params.path")
    log.debug ("loading resource from $fullPath")
    def resFile = new File(fullPath)

    if (resFile.canRead()) {
      def template = groovyPagesTemplateEngine.createTemplate(resFile)
       Writable result = template.make([params:params, config:grailsApplication.config])
       result.writeTo(new StringWriter())

      render (text:result.writeTo(new StringWriter()).getBuffer(), contentType:params.contentType, encoding:params.encode)
    }
    else
      response.sendError(404, fullPath)
  }


  def render = {
    if (!params.packid) {
      response.sendError("500", "missing mandatory parameter: 'packid'")
      return
    }
    params.encode = params.encode ?: "UTF-8"
    params.contentType = params.contentType ?: "text/css"

    def idlist = ResourceTagLib.openPack(session, params.packid)

    // find  latest modification date of all files in pack
    Date dtLastMod = (Date)idlist.inject(new Date(0)) {prevDate, item->
      def fullPath = request?.session?.servletContext?.getRealPath("$params.dir/$item.file")
      File f = new File(fullPath)

      if (f.exists()) {
        long l  = f.lastModified()
        Date dtl = new Date(l)
        if (dtl > prevDate)
          prevDate = dtl
      }
    }

    // check if any participating css files are modified in case it is requested by the browser (normally, it should)
    // and return a "304 Not Modified".
    def ifModifiedSince = request.getHeader("If-Modified-Since")
    if (ifModifiedSince) {
      def modDate = DateUtil.parseDate(ifModifiedSince, [DateUtil.PATTERN_ASCTIME, DateUtil.PATTERN_RFC1036, DateUtil.PATTERN_RFC1123])
      // forcefully strip milliseconds since the header format has only second precession
      if ((long)(dtLastMod.getTime()/1000) <= (long)(modDate.getTime()/1000)) {
        idlist.clear()
        render(status:304)
        return
      }
    }

    // set headers to enable caching
    enableCacheControl (dtLastMod)

    // render result (even if its empty)
    String result = ""
    idlist?.each {
      def fpath = "$params.dir/$it.file"
      def part = respack.include (path:fpath, parse:it.parse) ;
      result += part
    }
    idlist.clear()
    render (text:result, contentType:params.contentType, encoding:params.encode)
  }

  void enableCacheControl (Date dateMod) {
    def now = new Date(System.currentTimeMillis() + (60*60*24*1000))

    response.setHeader("Expires", DateUtil.formatDate(now, DateUtil.PATTERN_RFC1123))
    response.setHeader("Cache-Control", "public")
    response.setHeader("Vary", "Accept-Encoding")
    if (dateMod)
      response.addHeader("Last-Modified", DateUtil.formatDate(dateMod,DateUtil.PATTERN_RFC1123)) ;
  }




  def contentTypeFromPath(def path) {
    def parts = path?.split(/\./)
    def type = "text/plain"
    switch (parts[-1]) {
      case 'css':
        type = "text/css"; break;
      case 'javascript':
        type = "text/javascript"; break;
    }


  }
}
