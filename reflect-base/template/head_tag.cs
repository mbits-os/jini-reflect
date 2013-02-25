<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<?cs if:project.name ?>
  <meta name="description" content="Javadoc API documentation for <?cs var:project.name ?>." />
<?cs else ?>
  <meta name="description" content="Javadoc API documentation." />
<?cs /if ?>
<link rel="shortcut icon" type="image/x-icon" href="<?cs var:toroot ?>favicon.ico" />
<title>
<?cs if:page.title ?>
  <?cs var:page.title ?>
<?cs /if ?>
<?cs if:project.name ?>
| <?cs var:project.name ?>
<?cs /if ?>
</title>
<script src="<?cs var:toassets ?>search_autocomplete.js" type="text/javascript"></script>
<script src="<?cs var:toassets ?>jquery-resizable.min.js" type="text/javascript"></script>
<script src="<?cs var:toassets ?>prettify.js" type="text/javascript"></script>
<script type="text/javascript">
  setToRoot("<?cs var:toroot ?>", "<?cs var:toassets ?>");
</script><?cs 
if:reference ?>
<script src="<?cs var:toassets ?>doclava-developer-reference.js" type="text/javascript"></script>
<script src="<?cs var:toassets ?>navtree_data.js" type="text/javascript"></script><?cs 
/if ?>
<script src="<?cs var:toassets ?>customizations.js" type="text/javascript"></script>

<link rel="stylesheet" href="<?cs var:toassets ?>gh_pages/stylesheets/styles.css">
<link rel="stylesheet" href="<?cs var:toassets ?>gh_pages/stylesheets/pygment_trac.css">
<link rel="stylesheet" href="<?cs var:toassets ?>doclava.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
<script src="<?cs var:toassets ?>gh_pages/javascripts/respond.js"></script>
<!--[if lt IE 9]>
    <script src="//html5shiv.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<!--[if lt IE 8]>
<link rel="stylesheet" href="<?cs var:toassets ?>gh_pages/stylesheets/ie.css">
<![endif]-->
<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">

<noscript>
  <style type="text/css">
    html,body{overflow:auto;}
    #body-content{position:relative; top:0;}
    #doc-content{overflow:visible;border-left:3px solid #666;}
    #side-nav{padding:0;}
    #side-nav .toggle-list ul {display:block;}
    #resize-packages-nav{border-bottom:3px solid #666;}
  </style>
</noscript>
</head>
