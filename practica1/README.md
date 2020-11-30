# Practica 1: el entorno de trabajo

La idea de esta práctica es familiarizarse con la herramientas que vamos a usar en el curso.

## Ejercicio 1. Instalar ES y Kibana por medio de los tar.gz.

Vamos  famirializarnos con la intalación en un entorno Linux mediante tr.gz
### Ejercicio 1: Instalando ES y Kibana

1. Lo primero que vamos a hacer es crear un directorio para instalar ES en /opt.

```bash
$ sudo mkdir /opt/ES
$ sudo chown mbd. /opt/ES
```
y nos aseguramos que versión de Java tenemos instalada para cercionarnos que es superior a la versión 8
```bash
$ java -version
```
2. Nos descargamos la vesrión última de ES y de kibana. En el momento en el que se hizo esta guía es la version 7.10.0. Desde la site de ES:
www.elastic.co/downloads
En la parte de ElasticSearch y escogéis la opción LINUX_X86_64
```bash
$ cd /opt/ES
$ mv ~/Descargas/elasticsearch-7.10.0-linux-x86_64.tar.gz .
$ tar -xvzf  elasticsearch-7.10.0-linux-x86_64.tar.gz
$ ln -s elasticsearch-7.10.0 elastic

$ mv ~/Descargas/kibana-7.10.0-linux-x86_64.tar.gz .
$ tar -xvzf  kibana-7.10.0-linux-x86_64.tar.gz
$ ln -s kibana-7.10.0-linux-x86_64 kibana
```

3. Modificamos los ficheros de configuración de ES
Prestad atención a poner VUESTRA IP

Si no estáis acostumbrados a trabajar con vim, se puede usar nano o gedit. El editor con el que se este más cómodo.

```bash
$ vim /opt/ES/elastic/config/elasticsearch.yml
```

```yaml
cluster.name: MasterBigData
node.name: Warty Warthog
path.data: /var/data/ES/elastic
path.logs: /var/log/ES/elastic
network.host: X.X.X.X
http.port: 9200
discovery.seed_hosts: ["X.X.X.X"]
cluster.initial_master_nodes: ["Warty Warthog"]

```

4. Modificamos los ficheros de configuración de Kibana

```bash
$ vim /opt/ES/kibana/config/kibana.yml
```

```yaml
elasticsearch.hosts: ["http://X.X.X.X:9200"]
pid.file: /var/data/ES/kibana/run/kibana.pid
logging.dest: /var/log/ES/kibana/kibana.log
```


## Ejercicio 2. Lanzando ElasticSearch.

1. Antes de arrancar los servicios debemos de crear las carpeta de datos y de log y darle permisos para mi usuario, dado que sino, no podrá arrancar:

```bash
$ sudo mkdir -p /var/data/ES/elastic
$ sudo mkdir -p /var/data/ES/kibana/run
$ sudo chown -R mbd. /var/data/ES/
$ sudo mkdir -p /var/log/ES/elastic
$ sudo mkdir -p /var/log/ES/kibana
$ sudo chown -R mbd. /var/log/ES/
```
2. Vamos a arrancar ES y Kibana

```bash
$ /opt/ES/elastic/bin/elasticsearch
```
y en otra terminal

```bash
$ /opt/ES/kibana/bin/kibana
```

3. Para comprobar que tenemos levantado elastic search, en un navegador podremos introducir:

http://X.X.X.X.9200/_cluster/health?pretty

http://localhost:5601

3a. Si os aparece el mensaje de error: 
```bash
vm.max_map_count is too low...
```
Es que debéis modificar los parametros del SO para que funcione ES, esto lo podréis realizar ejecutando los siguientes comandos:
**para cambios temporales**

```bash
$ ulimit -n 65536
$ sysctl -w vm.max_map_count=262144
```

**para cambios persistidos, implica tener que reiniciar máquina**

Modificar el fichero /etc/sysctl.conf
```conf
vm.max_map_count=262144
```
Y el fichero /etc/security/limits.conf (poner vuestro usuario)
```conf
**mbd** - nofile 65536
```

4. Creamos nuestro entorno de trabajo

```bash
$ cd 
$ mkdir elk-bootcamp
$ cd elk-bootcamp
$ mkdir ejercicio2
$ cd ejercicio2
```

5. Para arrancar como demonio los servicios lo podremos hacer con 

```bash
$ /opt/ES/elastic/bin/elasticsearch -d

$ nohup /opt/ES/kibana/bin/kibana &
```

### Jugando con un dataset de ejemplo
**(TODOS LOS COMANDOS CURL ESTAN HECHOS PARA LOCALHOST, si has modificado poniendo TU IP, deberás de ponerla)**

1. Primero nos descargamos el dataset de ejemplo.

```bash
$ wget "https://raw.githubusercontent.com/elastic/elasticsearch/master/docs/src/test/resources/accounts.json"
```

2. Después ejecuta el siguiente comando.

```bash
$ curl -H "Content-Type: application/json" -XPOST "localhost:9200/bank/_doc/_bulk?pretty&refresh" --data-binary "@accounts.json"
```

2. Para comprobar que todos los datos se han cargado bien ejecuta el siguiente comando.

```bash
$ curl "localhost:9200/_cat/indices?v"
```

3. Para hacer una busqueda simple puedes ejecutar el siguiente comando.

```bash
$ curl -X GET "localhost:9200/bank/_search?q=*&sort=account_number:asc&pretty"

```

4. Este método es una forma alternativa de hacer la misma query

```bash
$ curl -X GET "localhost:9200/bank/_search?pretty" -H 'Content-Type: application/json' -d'
  {
    "query": { "match_all": {} },
    "sort": [
  { "account_number": "asc" }
    ]
  }'
  

```
### Ejemplos en Kibana
Para comprobar que todo esta arrancado deberemos de abrir un navegador y comprobar que la siguiente url está activa:
http://localhost:5601

1-. Ir a la parte de Stack Monitoring
¿Cómo se ven los índices existentes?

2-. Ir a la parte de Dev Tools y realizar las mismas consultas que hemos realizado anteriormente.

3-. Ir a la parte de Stack monitoring y activar la monitorización con metricBeats del servidor de ES.  Seguir los pasos de la web.
Activar la monitorización de elasticsearch, kibana y linux

4-. En kibana visualizar el valor de system.process.memory.size

5-. Ahora  volver a observar los indices que hay creados.

Los logs los podremos observar:
```bash
$ tail -f /var/log/ES/elastic/MasterBigData.log
$ tail -f /var/log/ES/kibana/kibana.log
```
## 

