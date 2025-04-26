const path = require("path");

export const CACHE_VERSION = 3;
export const CACHE_PATH = path.resolve(__dirname, ".tw-cache.json");
export const CONFIG_PATH = path.resolve(process.cwd(), "tailwind.config.js");
export const OUTPUT_PATH = path.resolve(__dirname, "tailwind-classes.json");
