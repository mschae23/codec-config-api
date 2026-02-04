# Codec config API
A simple versioned config API using Minecraft's `Codec` system that allows automatic updates of configs.

## Usage
Add the Maven repository to the `repositories` block near the top of your `build.gradle`:
```groovy
repositories {
    // ...
    maven {
        url = uri('https://code.mschae23.de/api/packages/mschae23/maven')
    }
}
```

Add the API as a dependency:
```groovy
dependencies {
    // ...
    modImplementation "de.mschae23:codec-config-api:$project.codec_config_api_version"
    include "de.mschae23:codec-config-api:$project.codec_config_api_version"
}
```

Define the `codec_config_api_version` key in `gradle.properties` to set the version. See [the list of published
versions](https://code.mschae23.de/mschae23/-/packages/maven/de.mschae23-codec-config-api/versions). You can usually
just use the newest one, disregarding Minecraft version, because the library doesn't break often.

## License
Codec config API
Copyright (C) 2026  mschae23

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
