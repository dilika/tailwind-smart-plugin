// lib/utils/regex.js
export const CLASS_REGEX = /\.([^{}:]*?)(?::[^{}]+)?(?=\s*\{)/g;
export const VARIANT_REGEX = /^([a-z-]+):(.+)$/;
export const ARBITRARY_REGEX = /^\[.*\]$/;
export const IMPORTANT_REGEX = /!important/;