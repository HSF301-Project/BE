/* ============================================================
   tailwind-config.js — Shared Tailwind CSS configuration
   Used by all pages that load the Tailwind CDN
   ============================================================ */
tailwind.config = {
    darkMode: "class",
    theme: {
        extend: {
            colors: {
                "surface-container":           "#ededf5",
                "on-tertiary-container":       "#7ecf79",
                "surface-variant":             "#e2e2ea",
                "on-tertiary-fixed":           "#002204",
                "surface-container-high":      "#e8e7f0",
                "primary-fixed-dim":           "#b0c6ff",
                "on-background":               "#1a1b21",
                "on-secondary-container":      "#541100",
                "secondary-fixed-dim":         "#ffb5a0",
                "outline-variant":             "#c3c6d4",
                "on-surface":                  "#1a1b21",
                "secondary-fixed":             "#ffdbd1",
                "secondary-container":         "#fe5825",
                "surface-container-low":       "#f3f3fb",
                "on-secondary-fixed-variant":  "#872100",
                "on-tertiary-fixed-variant":   "#005312",
                "on-error-container":          "#93000a",
                "tertiary-fixed-dim":          "#88d982",
                "surface-dim":                 "#d9d9e1",
                "tertiary-fixed":              "#a3f69c",
                "on-primary-fixed":            "#001945",
                "error-container":             "#ffdad6",
                "inverse-on-surface":          "#f0f0f8",
                "on-primary-container":        "#a1bbff",
                "secondary":                   "#b02e00",
                "on-primary":                  "#ffffff",
                "primary-container":           "#0d47a1",
                "inverse-primary":             "#b0c6ff",
                "tertiary-container":          "#005914",
                "error":                       "#ba1a1a",
                "on-error":                    "#ffffff",
                "surface":                     "#faf8ff",
                "primary":                     "#003178",
                "surface-container-highest":   "#e2e2ea",
                "on-surface-variant":          "#434652",
                "on-secondary":                "#ffffff",
                "on-primary-fixed-variant":    "#00429c",
                "inverse-surface":             "#2e3036",
                "on-tertiary":                 "#ffffff",
                "primary-fixed":               "#d9e2ff",
                "outline":                     "#737783",
                "surface-container-lowest":    "#ffffff",
                "tertiary":                    "#003f0b",
                "on-secondary-fixed":          "#3b0900",
                "surface-bright":              "#faf8ff",
                "background":                  "#faf8ff",
                "surface-tint":                "#2b5bb5"
            },
            borderRadius: {
                "DEFAULT": "0.25rem",
                "lg":      "0.5rem",
                "xl":      "0.75rem",
                "full":    "9999px"
            },
            fontFamily: {
                "headline": ["Plus Jakarta Sans", "sans-serif"],
                "body":     ["Inter", "sans-serif"],
                "label":    ["Inter", "sans-serif"]
            }
        }
    }
};
