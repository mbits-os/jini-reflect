<?cs def:custom_masthead() ?>
<div id="header">
    <nav>
        <?cs if:project.name ?>
        <h1><a id="linkHome" href="<?cs var:toroot ?>jini-reflect/"><span>Home</span></a>&nbsp;&raquo; <?cs var:project.name ?></h1>
        <?cs /if ?>
	<div id="headerTools">
            <?cs call:default_search_box() ?>
            <?cs if:reference && reference.apilevels ?>
                <?cs call:default_api_filter() ?>

            <?cs /if ?>
	</div>
    </nav>
</div><!-- header -->

<div class="wrapper">

    <section>
<?cs /def ?>