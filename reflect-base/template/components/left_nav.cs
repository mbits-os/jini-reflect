<?cs # The default side navigation for the reference docs ?><?cs 
def:custom_left_nav() ?>
  <div class="g-section g-tpl-240" id="body-content">
    <div class="g-unit g-first side-nav-resizable" id="side-nav">
      <div id="swapper">
        <div id="nav-panels">
          <div id="resize-packages-nav">
            <div id="packages-nav">
              <div id="index-links"><nobr>
                <a href="<?cs var:toroot ?>packages.html" <?cs if:(page.title == "Package Index") ?>class="selected"<?cs /if ?> >Package Index</a> | 
                <a href="<?cs var:toroot ?>classes.html" <?cs if:(page.title == "Class Index") ?>class="selected"<?cs /if ?>>Class Index</a></nobr>
              </div>
              <ul>
                <?cs call:package_link_list(docs.packages) ?>
              </ul><br/>
            </div> <!-- end packages -->
          </div> <!-- end resize-packages -->
          <div id="classes-nav"><?cs 
            if:subcount(class.package) ?>
            <ul>
              <?cs call:list("Interfaces", class.package.interfaces) ?>
              <?cs call:list("Classes", class.package.classes) ?>
              <?cs call:list("Annotations", class.package.annotations) ?>
              <?cs call:list("Enums", class.package.enums) ?>
              <?cs call:list("Exceptions", class.package.exceptions) ?>
              <?cs call:list("Errors", class.package.errors) ?>
            </ul><?cs 
            elif:subcount(package) ?>
            <ul>
              <?cs call:class_link_list("Interfaces", package.interfaces) ?>
              <?cs call:class_link_list("Classes", package.classes) ?>
              <?cs call:class_link_list("Annotations", package.annotations) ?>
              <?cs call:class_link_list("Enums", package.enums) ?>
              <?cs call:class_link_list("Exceptions", package.exceptions) ?>
              <?cs call:class_link_list("Errors", package.errors) ?>
            </ul><?cs 
            else ?>
              <script>
                /*addLoadEvent(maxPackageHeight);*/
              </script>
              <p style="padding:10px">Select a package to view its members</p><?cs 
            /if ?><br/>
          </div><!-- end classes -->
        </div><!-- end nav-panels -->
        <div id="nav-tree" style="display:none">
          <div id="index-links"><nobr>
            <a href="<?cs var:toroot ?>packages.html" <?cs if:(page.title == "Package Index") ?>class="selected"<?cs /if ?> >Package Index</a> | 
            <a href="<?cs var:toroot ?>classes.html" <?cs if:(page.title == "Class Index") ?>class="selected"<?cs /if ?>>Class Index</a></nobr>
          </div>
        </div><!-- end nav-tree -->
      </div><!-- end swapper -->
    </div> <!-- end side-nav -->
    <script>
      swapNav(); // tree view should be used on mobile
      if (!isMobile) {
        if ($("#nav-tree").is(':visible')) {
          init_default_navtree("<?cs var:toroot ?>");
        } else {
          addLoadEvent(function() {
            scrollIntoView("packages-nav");
            scrollIntoView("classes-nav");
          });
        }
      }
    </script><?cs 
/def ?>