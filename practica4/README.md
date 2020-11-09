# Práctica 4: usando Term-level queries

En esta práctica, vamos a probar a crear term-level queries o filtros para manejar documentos en ElasticSearch. 

## Ejercicio 1. Insertando documentos en ES

1. Para insertar eventos en ES lo vamos a hacer con un programa de python:

```bash
$ cd 
$ cd elk-bootcamp-esp
$ cd elk-bootcamp-esp
$ cd practica4/src/main/python
$ sudo pip3 install elasticsearch
$ python3.8 Carga_valores_iniciales.py

```
2. Y nos aseguramos que han sido insertados los datos en ElasticSearch por medio de Kibana en el indice eventos

3. Ahora buscaremos la estructura de los documentos insertados con kibana.
4. Podremos observar como el mapping de los campos T1 y T2 son de tipo date--> epoch_second.
5. Hacemos una búsqueda con un margen de tiempo, cogiendo un tiempo entre uno de los valores que aparecen en uno de los valores T1
:
```http
POST eventos/_search
{
  "query": {
    "bool": {
      "filter": [
        {
          "range": {
            "T1": {
              "gte": 1604920489,
              "lte": 1604920491,
            }
          }
        }
      ]
    }
  }
}

```
6. Realizamos la misma búsqueda pero con una fecha con formato humano

```http
POST eventos/_search
{
  "query": {
    "bool": {
      "filter": [
        {
          "range": {
            "T1": {
              "gte": "2020/11/09 11:13:00",
              "lte": "2020/11/09 11:16:00",
              "format": "yyyy/MM/dd HH:mm:ss"
            }
          }
        }
      ]
    }
  }
}

```
7. **¿Por qué podemos realizar esta consulta, si los datos no están almacenados en este formato nuevo?**

## Ejercicio 2. Entender el codigo de python y los métodos que contiene

## Ejercicio 3. Consultando en ES mediante funciones boleanas:

Queremos crear una consulta que nos devuelva los últimos n elementos de un timeline concreto que contengan algunos de los tags asignado y que estén antes de una fecha dada. 

Para poder resolver esta consulta vamos a utilizar una función boolean query. La boolean query contiene tres tipos de busqueda:
+ **Must queries.** Son filtros que el documento **DEBE** cumplir
+ **Should queries.** Son filtros que el documento **DEBERIA** cumplir. Para hacer que al menos una de consulta sea obligatoria tendremos que modificar el parámetro `minimumShouldMatch`.
+ **Must not queries.** Son filtros que el documento **NO DEBE** cumplir, los resultado que pasen este filtro serán excluidos de los resultados. 


2. Una vez hecho esto nos crearemos una instancia del Bool Query Builder y añadiremos el primer filtro **must** por event_id.

```http
POST eventos/_search
{
  "query": {
    "bool": {
      "must": [
        {"match": {
          "Event_ID": "A"
        }}
      ]
    }
  }
}
```

3. Por cada uno de los tags añadiremos un filtro **should**.

```http
POST eventos/_search
{
  "query": {
    "bool": {
      "must": [
        {"match": {
          "Event_ID": "A"
        }
        }
      ],
      "should":[
        {"match": {
          "Tag": "1"
          }
        },
        {"match": {
          "Tag": "2"
          }
        }
      ]
          
    }
  }
}

```

4. Y los ordenaremos por T1.

```bash
POST eventos/_search
{
  "query": {
    "bool": {
      "should": [
        {"match": {
          "Event_ID": "A"
        }
        },
        {"match": {
          "Event_ID": "B"
        }
        }
      ]
    }
  },
  "size": 20,
  "sort": [
    {
      "T1": {
        "order": "desc"
      }
    }
  ]
}
```

5. Modificamos el parámetro `minimumShouldMatch` para que el evento al menos contenga un tag., porque si no vemos que siguen apareciendo resultados del tag 3
```http
POST eventos/_search
{
  "query": {
    "bool": {
      "must": [
        {"match": {
          "Event_ID": "A"
        }
        }
      ],
      "should":[
        {"match": {
          "Tag": "1"
          }
        },
        {"match": {
          "Tag": "2"
          }
        }
      ], "minimum_should_match": 1
          
    }
  },
  "size": 20,
  "sort": [
    {
      "T1": {
        "order": "desc"
      }
    }
  ]
}
```



7. Fíjate que hemos ordenado la consulta en orden descendente, sin embargo queremos que el orden sea siempre ascendente ¿por qué hemos hecho esto?

## Ejercicio 4. Pasando los tests

1. Consulta que obtenga los ultimos 4 resultados ordenados del event_id= A
2. Consulta que obtenga los ultimos 10 resuldados ordenados por T2 que tengan tag 1 ó 3 y pertenezcan al evento_id = B
3. Consulta que obtenga los primeros 5 resultados ordenados por T1 que tengan un event_id = C y con tag = 2
4. Consulta de los resultados entre dos tiempos diferenciados entre 6 minutos.
5. Consulta de los resultados que no sean del tipo event_id = C

## Ejercicio5. Implementar en python en Carga_valores_iniciales alguna consulta usando los metodos del ES_Controller.
 

