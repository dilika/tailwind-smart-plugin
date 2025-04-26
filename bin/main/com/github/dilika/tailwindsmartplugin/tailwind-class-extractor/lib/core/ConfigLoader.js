// lib/core/ConfigLoader.js
import resolveConfigModule from 'tailwindcss/resolveConfig.js';
const resolveConfig = resolveConfigModule.default || resolveConfigModule;

import path from 'path';

export class ConfigLoader {
    constructor(projectRoot) {
        this.projectRoot = projectRoot;
    }

    async load() {
        const configPath = path.join(this.projectRoot, 'tailwind.config.js');
        const fileUrl = new URL(`file://${configPath}`);

        try {
            const module = await import(`${fileUrl.href}?t=${Date.now()}`);
            return module.default || module;
        } catch (e) {
            throw new Error(`Failed to load Tailwind config: ${e.message}`);
        }
    }
}