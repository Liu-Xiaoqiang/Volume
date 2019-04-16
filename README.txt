1.Copy audio.conf to /system/etc
2.Add follow to init.chipsee.sh

# HDMI Audio Init
HDMI=`cat /proc/cmdline | grep -c hdmi`
if [ $HDMI -eq 1 ]
then

CONFIG=/data/misc/audio/audio.conf
TEMPLATE=/system/etc/audio.conf

if [[ ! -f $CONFIG ]]
then
        mkdir /data/misc/audio
        chown system.system /data/misc/eth
        chmod 0777 /data/misc/audio
        cp $TEMPLATE $CONFIG
        chmod 0777 $CONFIG
        chown system.system $CONFIG
fi

HDMIVolume=`cat $CONFIG | busybox awk -F ":" '{print $2}' `
tinymix 4 $HDMIVolume

fi

3.Add HDMIVolume app to system.