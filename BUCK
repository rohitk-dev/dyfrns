include_defs('//bucklets/gerrit_plugin.bucklet')

gerrit_plugin(
  name = 'dyfrns',
  srcs = glob(['src/main/java/**/*.java']),
  resources = glob(['src/main/resources/**/*']),
  manifest_entries = [
    'Gerrit-PluginName: dyfrns',
    'Gerrit-ApiType: plugin',
    'Gerrit-ApiVersion: 2.13-SNAPSHOT',
    'Gerrit-Module: com.googlesource.gerrit.plugins.dyfrns.Module',
    'Gerrit-SshModule: com.googlesource.gerrit.plugins.dyfrns.SshModule',
    'Gerrit-HttpModule: com.googlesource.gerrit.plugins.dyfrns.HttpModule',
    'Gerrit-ReloadMode: restart',
  ],
)

# this is required for bucklets/tools/eclipse/project.py to work
java_library(
  name = 'classpath',
  deps = [':dyfrns__plugin'],
)

