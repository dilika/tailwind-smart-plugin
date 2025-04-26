import chokidar from 'chokidar';
import { debug } from '../utils/debug.js';
import path from 'path';

export class FileWatcher {
    constructor(projectRoot, patterns) {
        this.projectRoot = projectRoot;
        this.patterns = this.normalizePatterns(patterns);
        this.watcher = null;
        this.debug = debug.extend('file-watcher');
        this.changeCallbacks = new Set();
    }

    normalizePatterns(patterns) {
        return patterns.map(pattern =>
            path.isAbsolute(pattern)
                ? pattern
                : path.join(this.projectRoot, pattern)
        );
    }

    start() {
        if (this.watcher) return;

        this.debug(`Démarrage de la surveillance sur : ${this.patterns.join(', ')}`);

        this.watcher = chokidar.watch(this.patterns, {
            ignored: /(^|[/\\])\../, // Ignorer les fichiers cachés
            persistent: true,
            ignoreInitial: true,
            cwd: this.projectRoot,
            awaitWriteFinish: {
                stabilityThreshold: 500,
                pollInterval: 100
            }
        });

        this.setupEventHandlers();
    }

    setupEventHandlers() {
        this.watcher
            .on('add', file => this.handleChange('add', file))
            .on('change', file => this.handleChange('change', file))
            .on('unlink', file => this.handleChange('remove', file))
            .on('error', error => this.debug('Erreur de surveillance:', error))
            .on('ready', () => this.debug('Surveillance active'));
    }

    handleChange(event, filePath) {
        const relativePath = path.relative(this.projectRoot, filePath);
        this.debug(`Fichier ${event}: ${relativePath}`);

        for (const callback of this.changeCallbacks) {
            try {
                callback(event, filePath);
            } catch (error) {
                console.error('Erreur dans le callback de surveillance:', error);
            }
        }
    }

    onChange(callback) {
        this.changeCallbacks.add(callback);
        return () => this.changeCallbacks.delete(callback);
    }

    stop() {
        if (!this.watcher) return;

        this.debug('Arrêt de la surveillance');
        this.watcher.close();
        this.watcher = null;
        this.changeCallbacks.clear();
    }
}