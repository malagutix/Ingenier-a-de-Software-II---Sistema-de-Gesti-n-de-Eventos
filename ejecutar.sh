#!/bin/sh
# Compila y ejecuta el sistema (Linux / macOS)
cd "$(dirname "$0")" || exit 1
echo "Versión de Java instalada:"
java -version
rm -rf out
javac -encoding UTF-8 -cp lib/postgresql-42.7.4.jar -d out -sourcepath src src/app/Main.java src/app/DemoConsola.java || {
  echo "Hubo errores de compilación. Copiá el primer error y pasámelo."
  exit 1
}
java -Dfile.encoding=UTF-8 -cp "out:lib/postgresql-42.7.4.jar" app.Main
