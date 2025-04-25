import util from 'util';
import chalk from 'chalk';

const DEBUG_ENABLED = process.env.TAILWIND_DEBUG === 'true';
const LOG_LEVELS = {
    INFO: 'info',
    WARN: 'warn',
    ERROR: 'error',
    DEBUG: 'debug'
};

export function debug(...args) {
    if (!DEBUG_ENABLED) return;

    const message = util.format(...args);
    const timestamp = new Date().toISOString();
    console.log(`[${chalk.gray(timestamp)}] ${chalk.blue('DEBUG')} ${message}`);
}

export function log(level, ...args) {
    const message = util.format(...args);
    const timestamp = new Date().toISOString();

    const colors = {
        [LOG_LEVELS.INFO]: chalk.green,
        [LOG_LEVELS.WARN]: chalk.yellow,
        [LOG_LEVELS.ERROR]: chalk.red,
        [LOG_LEVELS.DEBUG]: chalk.blue
    };

    console.log(
        `[${chalk.gray(timestamp)}] ${colors[level](level.toUpperCase())} ${message}`
    );
}

function createDebugger(namespace) {
    return {
        log: (...args) => debug(`[${namespace}]`, ...args),
        info: (...args) => log(LOG_LEVELS.INFO, `[${namespace}]`, ...args),
        warn: (...args) => log(LOG_LEVELS.WARN, `[${namespace}]`, ...args),
        error: (...args) => log(LOG_LEVELS.ERROR, `[${namespace}]`, ...args),
        time: (label) => {
            if (!DEBUG_ENABLED) return;
            console.time(`[${namespace}] ${label}`);
        },
        timeEnd: (label) => {
            if (!DEBUG_ENABLED) return;
            console.timeEnd(`[${namespace}] ${label}`);
        }
    };
}

export { createDebugger };

// export const debug = createDebug;

// Export des méthodes principales