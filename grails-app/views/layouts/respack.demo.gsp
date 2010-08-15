<%--
  User: mkuhl
  Date: 15.08.2010
  Time: 11:49:47
--%>

<html>
    <head>
        <title>soft::machine respack plugin demo -  <g:layoutTitle default="index" /></title>
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />

        <respack:addcss packid="app-styles" file="common/reset.css" parse="false"/>
        <respack:addcss packid="app-styles" file="main/layout.css" parse="false"/>
        <respack:addcss packid="app-styles" file="main/default/customize.css" parse="true"/>

        <!-- start render app-styles -->
        <respack:renderpack  packid="app-styles" type="css" basedir="css" mode="onelink"/>
        <!-- end render app-styles-->


        <!-- javascript pack -->
        <respack:addscript packid="app-scripts" file="jquery-1.4.2.min.js" parse="false"/>
        <respack:addscript packid="app-scripts" file="jquery-ui-1.8.2.min.js" parse="false"/>
        <respack:addscript packid="app-scripts" file="jquery-superfish.js" parse="false"/>
        <respack:addscript packid="app-scripts" file="application.js" parse="false"/>
        <respack:renderpack  packid="app-scripts" type="script" basedir="js" mode="onelink"/>



        %{--<script type="text/javascript" src="${resource(dir:'js',file:'jquery-1.4.2.min.js')}"></script>--}%
        %{--<script type="text/javascript" src="${resource(dir:'js',file:'jquery-ui-1.8.2.min.js')}"></script>--}%
        %{--<script type="text/javascript" src="${resource(dir:'js',file:'jquery-superfish.js')}"></script>--}%
        %{--<script type="text/javascript" src="${resource(dir:'js',file:'application.js')}"></script>--}%

        %{--<g:javascript library="application" />--}%

        <g:layoutHead />
    </head>
    <body>
        <g:layoutBody />
    </body>
</html>