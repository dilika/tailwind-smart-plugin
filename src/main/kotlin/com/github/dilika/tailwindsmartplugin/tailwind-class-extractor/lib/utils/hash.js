import crypto from 'crypto';
import fs from 'fs/promises';

export function createHash(content, algorithm = 'sha256') {
    return crypto
        .createHash(algorithm)
        .update(content)
        .digest('hex');
}

export async function hashFile(filePath, algorithm = 'sha256') {
    try {
        const content = await fs.readFile(filePath, 'utf8');
        return createHash(content, algorithm);
    } catch (error) {
        console.error(`Erreur de hachage du fichier ${filePath}:`, error);
        return null;
    }
}

export function hashObject(obj, algorithm = 'sha256') {
    return createHash(JSON.stringify(obj), algorithm);
}

export function createContentHash(filesContent) {
    const combined = filesContent
        .map(({ file, content }) => `${file}:${content.length}`)
        .join('|');
    return createHash(combined);
}

