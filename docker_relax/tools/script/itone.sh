#!/bin/sh
sleep 20s
java -Xms1024m -Xmx2048m -Dfile.encoding=UTF-8 -Duser.timezone=GMT+08 -cp "./conf:./lib/3rd/*:./lib/app/*:./agents/dcs/lib/3rd/*:./agents/dcs/lib/app/*:./agents/dcs/lib/app/*:./server/lib/3rd/*:./server/lib/app/*:./web/lib/3rd/*:./web/lib/app/*"  com.its.itone.App >>/ruijie/relax/relax.log 2>&1 &
echo " * Starting Relax server"
