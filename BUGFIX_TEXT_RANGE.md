# 🐛 Correction du Bug TextRange

## Problème

Le plugin générait des centaines d'erreurs dans IntelliJ avec le message :
```
Argument rangeInElement (1344,1357) endOffset must not exceed descriptor text range (1260, 1392) length (132).
```

## Cause

Le calcul des offsets dans `extractClassesWithPositions` utilisait des **offsets absolus** (par rapport au document) au lieu d'**offsets relatifs** à l'élément.

Quand `registerProblem(element, textRange, ...)` est appelé, le `TextRange` doit être relatif à l'élément passé en premier paramètre, pas au document entier.

## Solution

### Avant ❌
```kotlin
// Calculait des offsets absolus
val startOffset = textRange.startOffset  // Offset absolu
classes.add(Triple(
    className,
    startOffset + classNameStart,  // Offset absolu - INCORRECT
    startOffset + classNameEnd
))
```

### Après ✅
```kotlin
// Calcule des offsets relatifs à l'élément
val elementTextRange = attributeValue.textRange  // Plage de l'élément entier
val valueTextRange = attributeValue.valueTextRange  // Plage du contenu
val contentStartInElement = valueTextRange.startOffset - elementTextRange.startOffset

val startOffsetRelative = contentStartInElement + classNameStartInString
val endOffsetRelative = contentStartInElement + classNameEndInString

// Vérification des limites
if (startOffsetRelative >= 0 && endOffsetRelative <= elementLength && 
    startOffsetRelative < endOffsetRelative) {
    classes.add(Triple(className, startOffsetRelative, endOffsetRelative))
}
```

## Changements

1. **Calcul des offsets relatifs** : Les offsets sont maintenant calculés relativement à l'élément entier (incluant les guillemets)
2. **Vérification des limites** : Ajout de vérifications pour s'assurer que les offsets sont dans les limites de l'élément
3. **Quick Fix corrigé** : Le quick fix convertit maintenant correctement les offsets relatifs à l'élément en offsets relatifs à la valeur

## Résultat

✅ **Plus d'erreurs** : Le plugin ne génère plus d'erreurs IntelliJ
✅ **Highlights précis** : Les classes invalides sont highlightées correctement
✅ **Quick fixes fonctionnels** : Les quick fixes fonctionnent correctement

## Fichiers Modifiés

- `src/main/kotlin/com/github/dilika/tailwindsmartplugin/inspection/TailwindInvalidClassInspection.kt`
  - Méthode `extractClassesWithPositions` : Calcul correct des offsets relatifs
  - Méthode `applyFix` dans `TailwindClassQuickFix` : Conversion correcte des offsets

---

*Bug corrigé le : $(date)*
*Build réussi : ✅*

