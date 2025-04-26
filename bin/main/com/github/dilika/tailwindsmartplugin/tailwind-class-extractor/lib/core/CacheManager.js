// lib/core/CacheManager.js
import fs from 'fs/promises';
import path from 'path';
import { createHash } from '../utils/hash.js';
import { debug } from '../utils/debug.js';

const CACHE_VERSION = 3;
const DEFAULT_CACHE_DIR = '.tailwind-intellij-cache';

export class CacheManager {
    constructor(projectRoot) {
        this.cachePath = path.join(projectRoot, DEFAULT_CACHE_DIR, 'plugin-cache.json');
        this.cache = {
            meta: {
                version: CACHE_VERSION,
                createdAt: new Date().toISOString()
            },
            data: {}
        };
    }

    async init() {
        try {
            await fs.mkdir(path.dirname(this.cachePath), { recursive: true });
            const rawData = await fs.readFile(this.cachePath, 'utf8');
            const parsed = JSON.parse(rawData);

            if (parsed.meta?.version === CACHE_VERSION) {
                this.cache = parsed;
                debug('Cache initialisé depuis le disque');
            } else {
                debug('Version de cache invalide, nouveau cache créé');
            }
        } catch (error) {
            debug('Aucun cache existant trouvé, initialisation nouvelle instance');
        }
    }

    async get(key) {
        this.validateKey(key);
        return this.cache.data[key];
    }

    async set(key, value) {
        this.validateKey(key);
        this.cache.data[key] = {
            value,
            lastUpdated: new Date().toISOString()
        };
        await this.persist();
    }

    async clear() {
        this.cache.data = {};
        await this.persist();
    }

    async isValid(key, currentHash) {
        const entry = await this.get(key);
        if (!entry) return false;

        return entry.value.hash === currentHash &&
            entry.value.timestamp > Date.now() - 86400000; // 24h cache
    }

    async updateConfigHash(hash, dependencies) {
        await this.set('tailwind-config', {
            hash,
            dependencies,
            timestamp: Date.now()
        });
    }

    // Méthodes privées
    async persist() {
        try {
            await fs.writeFile(
                this.cachePath,
                JSON.stringify(this.cache, null, 2),
                'utf8'
            );
            debug('Cache persisté sur le disque');
        } catch (error) {
            debug('Erreur de persistance du cache:', error.message);
        }
    }

    validateKey(key) {
        if (typeof key !== 'string') {
            throw new Error('Cache key must be a string');
        }
    }
}