autoreconf -i
export CFLAGS=-DEXEC_CONF_DIR=${worker-launcher.conf.dir} ${worker-launcher.additional_cflags}
./configure --prefix=/usr/local
export DESTDIR=${project.build.directory}/native/target
make install