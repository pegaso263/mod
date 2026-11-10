# Obsidian Evolution Set (Forge 1.21)

Esta carpeta contiene toda la estructura de código, modelos JSON, traducciones y texturas PNG para tu mod de Minecraft 1.21.

## Cómo compilar tu archivo .jar:
1. Descarga el MDK oficial de Forge 1.21 desde https://files.minecraftforge.net/net/minecraftforge/forge/index_1.21.html
2. Extrae el MDK en una carpeta y borra `src/main/java/com/example`.
3. Copia y pega el contenido de esta carpeta (`src` y `gradle.properties`) dentro de la carpeta del MDK, reemplazando cuando te lo pida.
4. Abre una terminal en esa carpeta y ejecuta:
   - Windows: `.\gradlew build`
   - Linux/Mac: `./gradlew build`
5. Tu mod compilado estará listo en `build/libs/obsidian_evo-1.0.0.jar`.
