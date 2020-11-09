# -*- coding: utf-8 -*-
""" Clase ES_Controller

Clase que engloba todas las operaciones posibles que el sistema
realizará sobre Elasticsearch

Además, contiene la definición de variables y constantes necesarias
para el sistema completo, tales como nombre de índices o datos de
conexión

@date: 02/11/2020
@version: 1.0.0
@author: Antonio
@status: Development
"""

from elasticsearch import Elasticsearch
from elasticsearch import helpers
from ES import Mappings
from Domain import Event

from Config import ES_HOST, ES_PORT, EVENT_INDEX


def openConn(host, port):
    """ Crea conexión a Elasticsearch

    Método para crear una nueva conexión a Elasticsearch,
    utilizando para ellos los datos recibidos como
    parámetros

    :param host:    (string) Nombre o dirección del servidor de
                    Elasticsearch
    :param port:    (int) Puerto utilizado para la conexión a ES
    :return:        (Elasticsearch) objeto de conexión a
                    Elasticsearch
    """

    return Elasticsearch([{'host': host, 'port': port}], timeout=300, max_retries=10, retry_on_timeout=True)  # Local


class ES_Controller(object):

    def __init__(self):
        """ Constructor de la clase

        """

        self.__es__ = openConn(ES_HOST, ES_PORT)



    def indexEvent(self, Events):
        """ Indexa un conjunto de eventos en Elasticsearch

        Método para realizar la indexación de un conjunto de eventos
        normalizados en Elasticsearch

        :param Event:   (list) Lista con el conjunto de usuareventos a
                        indexar
        """

        actions = []
        for event in Events:
            action = {
                "_index" : EVENT_INDEX,
                Event.Event.EVENT_ID:event.Event_ID,
                Event.Event.EVENT_TAG:event.Tag,
                Event.Event.EVENT_T1:event.T1,
                Event.Event.EVENT_T2:event.T2
            }
            actions.append(action)

        # Si no existe el índice, lo crea
        if not self.__es__.indices.exists(EVENT_INDEX):
            self.__es__.indices.create(EVENT_INDEX, body=Mappings.EVENT_MAPPING)

        # Se almacenan los eventos en ES
        if len(actions) > 0:
            helpers.bulk(self.__es__, actions)


    def obtainAllIndexDocuments(self, indexName, allDocuments=False, ID=0):
        """ Obtiene todos los documentos de un índice Elasticsearch

        Método que permite obtener el conjunto completo de documentos
        almacenados en un índice Elasticsearch

        Para ello utiliza los métodos "search" y "scroll" de la API

        Es posible restringir los elementos que se obtienen a partir
        de los parámetros booleanos que el método proporciona, de
        modo que se recuperen todos los elementos, o sólo squellos
        que han sido o no han sido exportados a Neurona

        :param indexName:           (string) Nombre del índice para
                                    el que se desea recuperar sus
                                    documentos
        :param allDocuments:        (bool) Indicador que determina si
                                    se deberá obtener el conjunto
                                    completo de documentos del índice
        :param ID:     (int) Si queremos buscar por un identificador. No tendrá
                                    efecto si allDocuments=True
        :return:                    (list) Lista con el conjunto de
                                    documentos recuperados. None si
                                    no existen documentos o el índice
        """
        result = []

        # Comprueba que existe el índice en ElasticSearch
        if self.__es__.indices.exists(indexName):
            # Establece el cuerpo de la consulta a Elasticsearch
            if allDocuments:
                searchBody = {}
            else:
                searchBody = {
                    "query":
                        {
                            "bool":
                                {
                                    "filter":
                                        {
                                            "term": {
                                                Event.Event.EVENT_ID: ID
                                            }
                                        }
                                }
                        }
                }

            # Se obtienen los datos haciendo uso de search & scroll
            data = self.__es__.search(indexName, scroll='5m', size=1000, body=searchBody)

            # Se obtiene el id del scroll y número de resultados
            sid = data['_scroll_id']
            scroll_size = len(data['hits']['hits'])

            # Se realizan consultas sucesivas hasta obtener todos los documentos del índice
            while scroll_size > 0:
                # Se procesan los resultados
                for hit in data['hits']['hits']:
                    result.append(hit)

                # Se obtiene el siguiente conjunto de datos
                data = self.__es__.scroll(scroll_id=sid, scroll='5m')

                # Se actualiza el scroll_id y número de resultados restantes
                sid = data['_scroll_id']
                scroll_size = len(data['hits']['hits'])

            return result
        else:
            return None

    def updateDocumentField(self, indexName, filterField, filterValue, updateField, updateValue):
        """ Actualiza un campo de un documento en un índice

        Actualiza el campo especificado, asignando el valor pasado
        como prámetro, para el documento que cumple la condición
        también proporcionada como parámetro en la llamada al método

        :param indexName:       (string) Nombre del índice
        :param filterField:     (string) Campo por el que se desea
                                filtrar
        :param filterValue:     (string) Valor concreto que se usa
                                como filtro, para el campo indicado
                                en "filterField"
        :param updateField:     (string) Campo que se debe actualizar
        :param updateValue:     (string) Valor a asignar al campo
                                que se desea actualizar
        """

        # Establece el cuerpo de la actualización
        source = str.format("ctx._source['{0}']='{1}'", updateField, updateValue) if type(updateValue) is str \
            else str.format("ctx._source['{0}']={1}", updateField, updateValue)
        updateBody = {
            "script": {
                "source": source,
                "lang": "painless"
            }, "query": {
                "match": {
                    filterField: filterValue
                }
            }
        }

        # Comprueba que existe el índice en ElasticSearch
        if self.__es__.indices.exists(indexName):
            # Actualiza el documento
            self.__es__.update_by_query(indexName, updateBody)


    def getEvent(self, Event_ID):
        """ Obtiene de Elasticsearch un documento de tipo Event

        Permite recuperar los datos correspondientes a un documento
        de tipo Event desde Elasticsearch, recibiendo para ello como
        parámetro el identificador del documento Event buscado

        :param Event_ID:    (string) Identificador del documento de
                            tipo Event solicitado
        :return:            (dict) Diccionario con los datos de la
                            Event requerida
        """
        indexName = EVENT_INDEX
        result = None

        # Comprueba que existe el índice en ElasticSearch
        if self.__es__.indices.exists(indexName):
            # Establece el cuerpo de la consulta a Elasticsearch

            searchBody = {
                "query": {
                    "bool": {
                        "must": [{
                            "match": {
                                Event.Event.EVENT_ID: Event_ID
                            }
                        }]
                    }
                }
            }

            # Se obtienen el documento solicitado
            eventData = self.__es__.search(indexName, searchBody)
            if eventData is not None:
                if eventData.get("hits").get("total").get("value") > 0:
                    result = eventData.get('hits').get('hits')[0].get('_source')

            return result

    def bulkIndex(self, Data, indexName, indexMapping=None):
        """ Indexa un conjunto de datos en Elasticsearch

        Método que permite realizar una indexación masiva (bulk) de
        datos en Elasticsearch

        :param Data:            (list) Conjunto de datos a indexar
        :param indexName:       (string) Nombre del índice en el que
                                se deben indexar los datos.
        :param indexMapping:    (string) Mapping del índice a usar
                                en caso de que deba generarse el
                                índice, por no existir previamente en
                                Elasticsearch
        """

        if indexName is not None:
            # Si no existe el índice, lo crea
            if not self.__es__.indices.exists(indexName):
                if indexMapping is not None:
                    self.__es__.indices.create(indexName, body=indexMapping)
                else:
                    self.__es__.indices.create(indexName)

        # Se almacenan las entidades en Elasticsearch
        if len(Data) > 0:
            helpers.bulk(self.__es__, Data)



    def checkIfDataExist(self, ID, identifierField, index):

        indexName = index

        # Comprueba que existe el índice en ElasticSearch
        if self.__es__.indices.exists(indexName):
            # Establece el cuerpo de la consulta a Elasticsearch
            searchBody = {
                "query": {
                    "bool": {
                        "must": [{
                            "match": {
                                identifierField: ID
                            }
                        }]
                    }
                }
            }

            # Se obtienen los datos del mensaje
            data = self.__es__.search(indexName, searchBody)
            if data is not None:
                if data.get("hits").get("total").get("value") > 0:
                    return True
                else:
                    return False
            else:
                return False

    def checkIfIndexExist(self, index):
        """ Comprueba la existencia de un índice en Elasticsearch

        :param index:      (string) Nombre del índice a comprobar
        :return:           (bool) True, si el índice existe
        """
        return self.__es__.indices.exists(index)

    def createIndex(self, indexName, indexMapping=None):
        """ Crea un nuevo índice en Elasticsearch

        Crea un nuevo índice en ElasticSearch, con la estructura del mapping
        que se recibe como parámetro

        :param indexName:      (string) Nombre del índice a crear
        :param indexMapping:   (string) Mapping del índice a crear

        :return:               (bool) True, si el índice existe
        """
        if indexMapping is not None:
            self.__es__.indices.create(indexName, body=indexMapping)
        else:
            self.__es__.indices.create(indexName)

    def bulkUpdate(self, indexName, filterValue, updateField, updateValue):
        """ Actualiza un campo de un documento en un índice

                Actualiza el campo especificado, asignando el valor pasado
                como prámetro en una lista en updateUser,

                :param indexName:       (string) Nombre del índice
                :param filterValue:     (list) valores _id que queremos actualizar
                :param updateField:     (string) Campo que se debe actualizar
                :param updateValue:     (string) Valor a asignar al campo
                                        que se desea actualizar
                """
        actions = []
        # Establece el cuerpo de la actualización
        for userGenID in filterValue:
            action = {
                "_op_type": 'update',
                "_index": indexName,
                "_id": userGenID,
                "doc": {
                    updateField: updateValue
                }
            }
            actions.append(action)

        # Comprueba que existe el índice en ElasticSearch
        if self.__es__.indices.exists(indexName):
            # Actualiza el documento
            if len(actions) > 0:
                helpers.bulk(self.__es__, actions)
