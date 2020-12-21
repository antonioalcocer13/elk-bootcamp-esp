# Práctica 2: operaciones básicas de ElasticSearch

El objetivo de esta práctica es aprender a utilizar los comandos básicos de ElasticSearch. Indexar, borrar, editar y buscar documentos almacenados en este motor de búsqueda.

## Ejercicio1. Monitorizando el estado de tu cluster.

La idea de este ejercicio es que conozcamos cómo extraer los principales parámetros de un cluster, para conocer su estado.

1. Lo primero que vamos es arrancar todo:
```bash
$ /opt/ES/elastic/bin/elasticsearch -d
$ /opt/ES/kibana/bin/kibana
```
2. El comando más básico para saber cual es el estado del cluster es el comando status.

```bash
$ curl -X GET "localhost:9200/_cluster/health?pretty"
```

3. Este comando devuelve la información general de como se encuentra el cluster.
4. Para conseguir toda la información relacionada con el cluster, podemos ejecutar la siguiente instrucción.

```bash
$ curl -X GET "localhost:9200/_cluster/state?pretty"
```

5. Esto devuelve una  gran cantidad de información podemos filtrarla indicando las métricas que queremos captura.

```bash
$ curl -X GET "localhost:9200/_cluster/state/metadata,routing_table/?pretty"
```

5. Para recuperar estadísticas del uso del cluster podemos ejecutar la siguiente petición.

```bash
$ curl -X GET "localhost:9200/_cluster/stats?human&pretty"
```

6. Para recuperar la configuración del cluster podemos ejecutar este comando.

```bash
$ curl -X GET "localhost:9200/_cluster/settings"
```

7. Listando los indices.

```bash
$ curl -X GET "localhost:9200/_cat/indices?v"
```

Podemos saber más información de los nodos, del estado de los share y demás  partes del cluster, para ello podemos ir a la API de ElasticSearch.

## Ejercicio2. Operaciones CRUD.

En este ejercicio vamos a repasar las operaciones de creación, lectura, actualización y borrado que podemos hacer con el motor de ElasticSearch.

### Creando y borrando un indice

Lo primero vamos a ver cómo podemos crear un indice en ElasticSearch.

1. Vamos a comprobar si los servicios siguen levantados:
```bash
$ jps
```

2. Vamos a crear un indice con el comando básico.

```bash
$ curl -X PUT "localhost:9200/twitter"
```

3. Este comando utiliza el numero de Shards y réplicas que asigna ElasticSearch por defecto. Si queremos crear un indicio con otro número de Shards o réplicas debemos especificarlo.

```bash
$ curl -X PUT "localhost:9200/twitter-2" -H 'Content-Type: application/json' -d'
{
    "settings" : {
        "number_of_shards" : 3,
        "number_of_replicas" : 2
    }
}'
```

4. Y si queremos especificar un Mapping Type especifico.

```bash
curl -X PUT "localhost:9200/test" -H 'Content-Type: application/json' -d'
{
    "settings" : {
        "number_of_shards" : 1
    },
    "mappings" : {
      "properties" : {
      "field1" : { "type" : "text" }
      }
    }
}'

```

5. Este comando deberemos meterlo en nuestro script de arranque, pero necesitaré un método para comprobar si  un indice existe o no.

```bash
$ curl --HEAD "localhost:9200/twitter"
```

6. Si listamos los indices podemos ver todos los que ahora mismos existen en el sistema.

```bash
$ curl -X GET "localhost:9200/_cat/indices?v"
```

7. Para borrar un indice podemos ejecutar el siguiente comando.

```bash
$ curl -X DELETE "localhost:9200/twitter"
```

8. **Tarea:** Borra todo los indices que hemos creado en este apartado.

### Indexando con  documentos

En este apartado vamos a indexar algunos momento y probaremos como podemos  crear nuestro documentos.

1. Vamos a crear un documento.

```bash
$ curl -X PUT "localhost:9200/twitter/1" -H 'Content-Type: application/json' -d'
{
    "user" : "kimchy",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "trying out Elasticsearch"
}'
```
¿Por que falla? pista: https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-index_.html
2. Pero espera nos habíamos cargado el indice. ¿qué ha pasado?
3. Vale pero hemos creado un documento donde hemos puesto el ID de forma explicita, ahora vamos a probar esto.

```bash
$ curl -X POST "localhost:9200/twitter/_doc" -H 'Content-Type: application/json' -d'
{
    "user" : "kimchy",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "trying out Elasticsearch"
}'
```

4. En este caso el id es auto-generado por lo que no nos tenemos que preocupar de su generación.
5. Por último vamos a borrar el indice twitter.

```bash
$ curl -X DELETE "localhost:9200/twitter"
```

4. **Pregunta:** ¿Cómo se almacenan los documentos?

### Recuperando documentos

Vamos a recuperar los documentos a través de su ID por lo que no haremos consultas complejas, pero entenderemos como se almacenan nuestros datos en ElasticSearch.

1. Vamos a crear un documento.

```bash
$ curl -X PUT "localhost:9200/twitter/_doc/0" -H 'Content-Type: application/json' -d'
{
    "user" : "kimchy",
    "post_date" : "2009-11-15T14:12:12",
    "message" : "trying out Elasticsearch"
}'
```

2. Primero vamos a chequear que nuestro documento exista.

```bash
$ curl --HEAD "localhost:9200/twitter/_doc/0"
```

3. Para recuperarlo por su ID vamos a utilizar el siguiente comando.

```bash
$ curl -X GET "localhost:9200/twitter/_doc/0?pretty"
```

4. Espera aquí hay más cosas de las que hemos añadido. ¿Para qué sirven todos esto datos?
5. Esto es simple pero la recuperación de información se complica cuándo lanzamos consultas directas a los indices.

### Borrando un documento

Borrar documentos es sencillo en ElasticSearch y no es necesario tener que borrar siempre el indice.

1. Esto es muy sencillo solo tenemos que lanzar este comando y borramos el documento seleccionado.

```bash
$ curl -X DELETE "localhost:9200/twitter/_doc/0"
```

2. Pero sí queremos borrar varios documento y pero no queremos borrar el indice, debemos utilizar el borrado por query.

```bash
$ curl -X POST "localhost:9200/twitter/_delete_by_query" -H 'Content-Type: application/json' -d'
{
  "query": { 
    "match_all": {}
  }
}'

```

## Ejercicio 3. Probando el lenguaje de consultas.

En este ejercicio vamos a aprender cómo ejecutar diferentes tipos de consulta utilizando el Query DSL.

1. Vamos a cargar un juego de datos en ElasticSearch para que podamos jugar con los datos del sistema.

```bash
$ curl -H "Content-Type: application/json" -XPOST "localhost:9200/bank/_doc/_bulk?pretty&refresh" --data-binary "@accounts.json"
```

2. Si hacemos `head accounts.json` podemos ver que formato tiene estos datos.

```json
{"index":{"_id":"1"}}
{"account_number":1,"balance":39225,"firstname":"Amber","lastname":"Duke","age":32,"gender":"M","address":"880 Holmes Lane","employer":"Pyrami","email":"amberduke@pyrami.com","city":"Brogan","state":"IL"}
{"index":{"_id":"6"}}
{"account_number":6,"balance":5686,"firstname":"Hattie","lastname":"Bond","age":36,"gender":"M","address":"671 Bristol Street","employer":"Netagy","email":"hattiebond@netagy.com","city":"Dante","state":"TN"}
{"index":{"_id":"13"}}
{"account_number":13,"balance":32838,"firstname":"Nanette","lastname":"Bates","age":28,"gender":"F","address":"789 Madison Street","employer":"Quility","email":"nanettebates@quility.com","city":"Nogal","state":"VA"}
{"index":{"_id":"18"}}
{"account_number":18,"balance":4180,"firstname":"Dale","lastname":"Adams","age":33,"gender":"M","address":"467 Hutchinson Court","employer":"Boink","email":"daleadams@boink.com","city":"Orick","state":"MD"}
{"index":{"_id":"20"}}
{"account_number":20,"balance":16418,"firstname":"Elinor","lastname":"Ratliff","age":36,"gender":"M","address":"282 Kings Place","employer":"Scentric","email":"elinorratliff@scentric.com","city":"Ribera","state":"WA"}
```

3. Otra forma de saber como son los datos que tenemos en recuperando el Mapping Type.

```bash
$ curl -X GET "localhost:9200/bank/_mapping/"
```

4. Ahora vamos a hacer la query sencilla vamos a contar cuantos registros hay.

```bash
$ curl -X GET "localhost:9200/bank/_count" -H 'Content-Type: application/json' -d'
{
    "query": {
        "match_all": {}
    }
}'
```

5. Pregunta: ¿Cuántas mujeres hay en la empresa?

```bash
$ curl -X GET "localhost:9200/bank/_count" -H 'Content-Type: application/json' -d'
  {
      "query": {
      "match": {"gender":"F"}
      }
  }'

```

6. Pregunta: ¿Cuántas mujeres viven en MA or WA?

```bash
$ curl -X GET "localhost:9200/bank/_count" -H 'Content-Type: application/json' -d'
  {
      "query": {
       "bool": {
            "filter": {
                  "match": {"gender": "f"}
            },
              "should": [
                  {"match": { "state":"WA"}},
                  {"match": { "state":"MA"}}
               ],
              "minimum_should_match" : 1
       }
      }
  }'

```

7. Pregunta: ¿Cuántos hombres tiene un saldo mayor  que 30000$?

```bash
$ curl -X GET "localhost:9200/bank/_count" -H 'Content-Type: application/json' -d'
  {
      "query": {
       "bool": {
          "must": [{
                  "match": {
                   "gender": "m"
                  }},
                  {"range": {
                      "balance": {
                          "from":30000
                      }
                  }}
                  ]
            }
           }
   }    
  }'

```



8. Pregunta: ¿Podemos borrar sólo los hombres por debajo de los 5000$?

```bash
$ curl -X POST "localhost:9200/bank/_delete_by_query" -H 'Content-Type: application/json' -d'
  {
      "query": {
       "bool": {
            "must": [{
                  "match": {
                 "gender": "m"
                  }},
                  {"range": {
                      "balance": {
                          "lte":5000
                      }
                  }
                  }
                  ]
           }
           }
   }    
  }'

```

9. Pregunta: ¿Cuántas mujeres tienes más de 30 años?

```bash
$ curl -X GET "localhost:9200/bank/_count" -H 'Content-Type: application/json' -d'
  {
      "query": {
      "bool": {
           "must": [{
                  "match": {
                   "gender": "f"
                  }},
                  {"range": {
                      "age": {
                          "gte":30
                      }
                  }}
                  ]
         }
           }
   }    
  '

```
10-. Para que las hagáis vosotros:
a-. ¿Cuántas mujeres del estado de Arkansas tienen mas de 30 años?
b-. ¿Cuantas mujeres de la ciudad de Nueva York tienen mas de 30000 $?
c-. ¿Cual es el salario más alto?
d-. ¿Cuantos hombres hay que tengan más de 50 años con unos ahorros inferiores a 10000$?
e-. ¿Cúantas cuentas tenemos abiertas en el estado de Massachusetts?
## Ejercicio 4. Practicando con ES y Kibana.
En esta ocasión en lugar de utilizar un terminal, vamos a hacer uso de kibana. Pero por ahora solo lo utilizaremos para poder realizar consultas de una forma "un poco más amigable".
1. Nos aseguramos que estén levantados los servicios.

```bash
$ jps
```

2. Nos conectamos a un navegador y nos conectamos a la URL:
http://localhost:5601

3. las consultas las realizaremos en Kibana en el partado de DEV_TOOLS
4. Vamos a hacer unas consultas similares a las que realizabamos anteriormente con curl:
```rest
GET _cat/indices?v
GET _cat/health?v
GET _cat/nodes?v
GET _cat/indices?v
PUT customer?pretty
GET _cat/indices?v
```
El quinto comando me crea un índice llamado “customer”, lo de pretty es para decirle que vamos a interactuar con él por medio de JSON, es decir, que nos devuelva los datos en formato JSON.
Crear:
```rest
POST customer/external/1
{ 
"name": "John Doe" 
}
```
Borrar
```rest
DELETE customer?pretty
```

```rest
PUT customer/_create/1?pretty
{ 
	"name": "John Doe" 
}
```
También funciona con POST

Recuperar
```rest
GET customer/_doc/1/?pretty
```
Actualizar
```rest
POST customer/_update/1
{ 
   "doc" : {
        "name": "Jane Doe" 
    }
}
```
Actualizar con más campos:
```rest
POST customer/_create/2
{ 
   "doc" : {
        "name": "Antonio Perez" 
    }
}
```
```rest
POST customer/_update/2/
{ 
	"doc": { "name": "Miguel Perez", "age": 20 } 
}
```
```rest
DELETE customer?pretty
```

### Trabajando con más datos.
1. Nos descargamos los datos de account2 a una carpeta de nuestro entorno de trabajo:
```bash
$ cd 
$ cd elk-bootcampo
$ mkdir ejerecicio3
$ cd ejercicio3
$ wget "https://raw.githubusercontent.com/antonioalcocer13/elk-bootcamp-esp/python/practica2/accounts2.json"
```

2. Con el ejemplo account2.json lo introducimos en la pestaña de ES de MachineLearning. inddicando que el nombre del índice sea bank. El formato de los datos será:

```json
{ 
"account_number": 0, 
"balance": 16623, 
"firstname": "Bradshaw", 
"lastname": "Mckenzie", 
"age": 29, 
"gender": "F", 
"address": "244 Columbus Place",
"employer": "Euron", 
"email": "bradshawmckenzie@euron.com", 
"city": "Hobucken", 
"state": "CO" 
} 
```
2. Hacemos una consulta sobre todos los resultados: 
```jql
SELECT * FROM bank;
```
Pero con el lenguaje de ES:
```rest
GET bank/_search?q=*&pretty
```
3. Siempre por defecto salen 10 resultados, aunque podemos limitar el numero de resultados con el atributo size
```rest
GET bank/_search?q=*&size=3&pretty
```
4. Y podemos hacer búsquedas simples:
```rest
GET bank/_search?q=*&sort=account_number:asc&pretty
```
La misma query la podemos hacer con:
```rest
GET bank/_search?pretty
{
    "query": { 
        "match_all": 
            {} 
    },
    "sort": [
        { 
            "account_number": "asc" 
        }
    ]
}
```
### Trabajando con búsquedas más complejas.
1. resultados desde el índice 10 los 20 siguientes:
```rest
POST bank/_search?pretty
{
    "query": { 
        "match_all": {} 
    },
    "from": 10,
    "size": 10
}
```
¿Por qué salen los números de cuenta de la 54 en adelante?

2. resultados qus solo me devuelve dos campos:
```rest
POST bank/_search?pretty
{ 
	"query": { "match_all": {} }, 
	"_source": ["account_number", "balance"] 
}
```
3. resultados qus me devuelve el que tiene el account_number = 20:
```rest
POST bank/_search?pretty
{
	"query": { "match": { "account_number": 20 } }
}
```
4. resultados con el documento que tenga la dirección con mill:
```rest
POST bank/_search?pretty
{ 
	"query": { "match": { "address": "mill" } } 
}
```

No aperece nada, sin embargo si buscamos: 990 Mill Road si que aperece. Si observamos   el mapping, veremos que por defecto nos lo ha puesto que el campo address es de tipo keyword. Lo que significa que debe de coincidir exactamente. Por lo que deberemos de cambiar el mapping. Borramos el indice y generamos el mapping antes de insertar los datos.
```rest
POST bank/_search?pretty
{ 
	"query": { "match": { "address": "990 Mill Road" } } 
}
```
5. Borramos el indice para actualizar el mapping correctamente:
```rest
DELETE bank
```
6. al crear el indice en machineLearning le ponemos el mapping siguiente:
```json
{
  "properties": {
    "account_number": {
        "type": "long"
      },
      "address": {
        "type": "text",
        "fields": {
          "keyword": { 
            			"type":  "keyword"
         		 }
        	}
     },
      "age": {
        "type": "long"
      },
      "balance": {
        "type": "long"
      },
      "city": {
        "type": "text",
        "fields": {
          "keyword": { 
            			"type":  "keyword"
         		 }
        	}
      },
      "email": {
        "type": "text",
         "fields": {
          "keyword": { 
            			"type":  "keyword"
         		 }
        	}
      },
      "employer": {
        "type": "text",
         "fields": {
          "keyword": { 
            			"type":  "keyword"
         		 }
        	}
      },
      "firstname": {
        "type": "keyword"
      },
      "gender": {
        "type": "keyword"
      },
      "lastname": {
        "type": "keyword"
      },
      "state": {
        "type": "keyword"
      }
  
  }
  }
```
7. Volvemos a hacer las consultas de antes.
8. Para hacer que se cumpla explícitamente lo que búscamos, iremos al campo .keyword:
```rest
POST bank/_search?pretty
{ 
	"query": { "match": { "address.keyword": "990 Mill Road" } } 
}
```
y sino, podremos buscar por tipo text y que me devuelva resultados similares:
```rest
POST bank/_search?pretty
{ 
	"query": { "match": { "address": "990 Mill Road" } } 
}
```
***¿Por qué hay diferencias entre los dos resultados?***

9. resultados que tenga la dirección con mill O lane
```rest
POST bank/_search?pretty
{ 
	"query": { "match": { "address": "mill lane" } } 
}
```
10. resultados que tenga la dirección con la frase “mill lane”:
```rest
POST bank/_search?pretty
{ 
	"query": { "match_phrase": { "address": "mill lane" } } 
}
```
Como veis, cuando vamos por text, no distingue entre mayúsculas ni minúsculas.

11. resultados que tenga la dirección con el dato “mill” ***Y*** “lane” ***SOLO***:
```rest
POST bank/_search?pretty
{ 
	"query": 
		{ "bool": { 
			"must": [ 
				{ "match": { "address": "mill" } }, 
				{ "match": { "address": "lane" } } 
			]
		 } 
	} 
}
```
12. resultados que tenga la dirección con el dato “mill” ***O*** “lane” ***SOLO***:
```rest
POST bank/_search?pretty
{ 
	"query": 
		{ "bool": { 
			"should": [ 
				{ "match": { "address": "mill" } }, 
				{ "match": { "address": "lane" } } 
			]
		 } 
	} 
}
```
13. resultados que NO tenga la dirección con el dato “mill” NI “lane”
```rest
POST bank/_search?pretty
{ 
	"query": 
		{ "bool": { 
			"must_not": [ 
				{ "match": { "address": "mill" } }, 
				{ "match": { "address": "lane" } } 
			]
		 } 
	} 
}
```
14. resultados que tengan 40 anos y que no vivan en el condado ID (Idaho):
```rest
POST bank/_search?pretty
{
    "query": {
        "bool": {
            "must": [
                { "match": { "age": "40" } }
            ],
            "must_not": [
                { "match": { "state": "ID" } }
            ]
        }
    }
}
```
### Trabajando con filtros.
1. resultados que tienen entre 20.000 y 30.000 en su cuenta:
```rest
POST bank/_search?pretty
{ 
	"query": { 
		"bool": { 
			"must": { "match_all": {} }, 
			"filter": { 
				"range": { 
					"balance": { 
						"gte": 20000, 
						"lte": 30000 }
					} 
				} 
			} 
		} 
}
```
### Trabajando con agregaciones.
1. Resultados agregados por estado:
```rest
POST bank/_search?pretty
{
    "size": 0,
    "aggs": {
        "group_by_state": {
            "terms": {
                "field": "state"
            }
        }
    }
}
```
Similar a la consulta en SQL:
```jql
SELECT state, COUNT(*) FROM bank GROUP BY state ORDER BY COUNT(*) DESC
```

# Ejercicios propuestos.
1. Pregunta: ¿Cuántas mujeres tienen cuenta que hayan ahorrado más de 30000$?
2. Pregunta: ¿Cuántas mujeres viven en el estado de Nevada or en el estado de California?
3. Pregunta: ¿Cuántos hombres tienen cuenta en el estado de Idaho?
4. Pregunta: ¿Cuántas mujeres se llaman Hattie y viven en el estado de Tennessee? 
5. Pregunta: ¿Agregar por empresas las cuantas que tenemos contratadas y ordenarlas por orden de clientes?

6-. Hallar el número de clientes que tienen entre 30 y 40 años y que tienen ahorrado entre 30000 y 40000$ para ofrecerles ofertas.

7-. Agregar por cuidades donde tenemos clientes con más de 20000$ ahorrados.

8-. ¿Cuántos clientes son mujeres y/o viven en el estado de california y/o tienen entre 20 y 30 años?
## 