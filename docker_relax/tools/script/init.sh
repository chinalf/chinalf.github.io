#!/bin/sh

echo 'version:' + $1 + $2
echo '------------update-----------'

java -Xms128m -Xmx256m -cp groovy-all-2.4.6-indy.jar org.codehaus.groovy.tools.GroovyStarter --classpath groovy-all-2.4.6-indy.jar --main  groovy.ui.GroovyMain -c utf8 UpgradeTool.groovy itone_atom.zip,itone_upgrade.zip,itone.zip,itone_cmdb.zip,itone_security.zip,itone_insight.zip,itone_km.zip,itone_msg.zip,itone_workflow.zip,itone_workorder.zip,itone_workorderwx.zip,itone_wx.zip $1 $2

wget -q -P /ruijie/relax/program/plugins http://172.17.189.71/itone/repos/$2/plugins/itone-plugins-bmc.zip
wget -q -P /ruijie/relax/program/plugins http://172.17.189.71/itone/repos/$2/plugins/itone-plugins-project.zip
wget -q -P /ruijie/relax/program/plugins http://172.17.189.71/itone/repos/$2/plugins/itone-plugins-callcenter.zip
wget -q -P /ruijie/relax/program/plugins http://172.17.189.71/itone/repos/$2/plugins/itone-plugins-insight.zip
wget -q -P /ruijie/relax/program/plugins http://172.17.189.71/itone/repos/$2/plugins/itone-plugins-msgtoemail.zip

cp itone.sh ./program/itone.sh
