// Populate the sidebar
//
// This is a script, and not included directly in the page, to control the total size of the book.
// The TOC contains an entry for each page, so if each page includes a copy of the TOC,
// the total size of the page becomes O(n**2).
class MDBookSidebarScrollbox extends HTMLElement {
    constructor() {
        super();
    }
    connectedCallback() {
        this.innerHTML = '<ol class="chapter"><li class="chapter-item expanded "><a href="index.html"><strong aria-hidden="true">1.</strong> gitflow-oss-java-slim</a></li><li class="chapter-item expanded "><a href="00-build-system/index.html"><strong aria-hidden="true">2.</strong> Build System Overview</a></li><li><ol class="section"><li class="chapter-item expanded "><a href="00-build-system/branching-strategy.html"><strong aria-hidden="true">2.1.</strong> Branching Strategy/Versioning</a></li></ol></li><li class="chapter-item expanded "><a href="01-action-parameters/index.html"><strong aria-hidden="true">3.</strong> Github Action Parameters</a></li><li class="chapter-item expanded "><a href="02-org-config/index.html"><strong aria-hidden="true">4.</strong> Org Config Specification</a></li><li><ol class="section"><li class="chapter-item expanded "><a href="02-org-config/maven-repositories.html"><strong aria-hidden="true">4.1.</strong> Maven Publication Repositories</a></li><li class="chapter-item expanded "><a href="02-org-config/development-features.html"><strong aria-hidden="true">4.2.</strong> Development Features Configuration</a></li><li class="chapter-item expanded "><a href="02-org-config/config-evolution.html"><strong aria-hidden="true">4.3.</strong> Configuration Evolution</a></li></ol></li><li class="chapter-item expanded "><a href="03-gradle-features/index.html"><strong aria-hidden="true">5.</strong> Gradle Features Overview</a></li><li class="chapter-item expanded "><a href="04-bootstrap/index.html"><strong aria-hidden="true">6.</strong> Org Config / Project bootstrap cheat-sheet</a></li></ol>';
        // Set the current, active page, and reveal it if it's hidden
        let current_page = document.location.href.toString().split("#")[0].split("?")[0];
        if (current_page.endsWith("/")) {
            current_page += "index.html";
        }
        var links = Array.prototype.slice.call(this.querySelectorAll("a"));
        var l = links.length;
        for (var i = 0; i < l; ++i) {
            var link = links[i];
            var href = link.getAttribute("href");
            if (href && !href.startsWith("#") && !/^(?:[a-z+]+:)?\/\//.test(href)) {
                link.href = path_to_root + href;
            }
            // The "index" page is supposed to alias the first chapter in the book.
            if (link.href === current_page || (i === 0 && path_to_root === "" && current_page.endsWith("/index.html"))) {
                link.classList.add("active");
                var parent = link.parentElement;
                if (parent && parent.classList.contains("chapter-item")) {
                    parent.classList.add("expanded");
                }
                while (parent) {
                    if (parent.tagName === "LI" && parent.previousElementSibling) {
                        if (parent.previousElementSibling.classList.contains("chapter-item")) {
                            parent.previousElementSibling.classList.add("expanded");
                        }
                    }
                    parent = parent.parentElement;
                }
            }
        }
        // Track and set sidebar scroll position
        this.addEventListener('click', function(e) {
            if (e.target.tagName === 'A') {
                sessionStorage.setItem('sidebar-scroll', this.scrollTop);
            }
        }, { passive: true });
        var sidebarScrollTop = sessionStorage.getItem('sidebar-scroll');
        sessionStorage.removeItem('sidebar-scroll');
        if (sidebarScrollTop) {
            // preserve sidebar scroll position when navigating via links within sidebar
            this.scrollTop = sidebarScrollTop;
        } else {
            // scroll sidebar to current active section when navigating via "next/previous chapter" buttons
            var activeSection = document.querySelector('#sidebar .active');
            if (activeSection) {
                activeSection.scrollIntoView({ block: 'center' });
            }
        }
        // Toggle buttons
        var sidebarAnchorToggles = document.querySelectorAll('#sidebar a.toggle');
        function toggleSection(ev) {
            ev.currentTarget.parentElement.classList.toggle('expanded');
        }
        Array.from(sidebarAnchorToggles).forEach(function (el) {
            el.addEventListener('click', toggleSection);
        });
    }
}
window.customElements.define("mdbook-sidebar-scrollbox", MDBookSidebarScrollbox);
