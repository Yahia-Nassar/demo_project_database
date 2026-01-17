(() => {
    const normalizePath = (path) => {
        if (!path) {
            return "";
        }
        return path.length > 1 ? path.replace(/\/+$/, "") : path;
    };

    const applyActiveNav = () => {
        const currentPath = normalizePath(window.location.pathname);
        const containers = document.querySelectorAll(".nav-links");

        containers.forEach((container) => {
            const links = container.querySelectorAll("a[data-path]");
            let matched = false;

            links.forEach((link) => {
                const basePath = normalizePath(link.getAttribute("data-path"));
                if (!matched && basePath && currentPath.startsWith(basePath)) {
                    link.classList.add("is-active");
                    matched = true;
                }
            });
        });
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", applyActiveNav);
    } else {
        applyActiveNav();
    }
})();