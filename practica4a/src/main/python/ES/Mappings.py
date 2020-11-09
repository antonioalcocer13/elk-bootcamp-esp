# -*- coding: utf-8 -*-
""" Definición de mappings para Elasticsearch

Módulo que contiene la definición de mappings generales para
la carga de información en Elasticsearch

@date: 27/12/2019
@version: 1.1.0
@author: Luciente, Ribadas
@status: Development
"""

from Domain.Event import Event

EVENT_MAPPING = {
    "mappings": {
        "properties": {
            Event.EVENT_ID: {
                "type": "keyword"
            },
            Event.EVENT_T1: {
                "type": "date",
                "format": "epoch_second"
            },
            Event.EVENT_T2: {
                "type": "date",
                "format": "epoch_second"
            },
            Event.EVENT_TAG: {
                "type": "text",
                "fields": {
                    "keyword": {
                        "type": "keyword",
                        "ignore_above": 1024
                    }
                }
            }
        }

    }
}
