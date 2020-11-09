# -*- coding: utf-8 -*-
""" Script para cargar datos prueba
@date: 02/11/2011
@version: 1.1.0
@author: Antonio
@status: Development
"""
import concurrent.futures
import datetime
import os
import time
import timeit
import json
import logging.config
import glob
from concurrent.futures import ThreadPoolExecutor
from functools import partial
from os.path import isfile, join
from Domain import Event
import Config


from ES.ES_Controller import ES_Controller

loads = {}
ElasticController = ES_Controller()


def setup_logging():
    """Setup logging configuration

    """
    path = Config.log_config_file

    if os.path.exists(path):
        with open(path, 'rt') as f:
            config = json.load(f)
        logging.config.dictConfig(config)
    else:
        logging.basicConfig(level=logging.INFO)




"""
Bucle principal de carga

En primer lugar se crea una instancia de cada Controller que puede hacer falta a lo largo del proceso.

"""

setup_logging()
logger = logging.getLogger(__name__)

logger.info("Inicio de la ejecución del cargador de ejemplo")

logger.info("Controladores creados")

# le restamos a la fecha actual 40 minutos
initTime = int(time.time()-2400)

eventos = []
ev = Event.Event("A","1", initTime+60, initTime+120)
eventos.append(ev)
ev = Event.Event("B", "2", initTime+180, initTime+240)
eventos.append(ev)
ev = Event.Event("A", "3", initTime+300, initTime+360)
eventos.append(ev)
ev = Event.Event("B","1", initTime+420, initTime+480)
eventos.append(ev)
ev = Event.Event("A","2", initTime+540, initTime+600)
eventos.append(ev)
ev = Event.Event("B","3", initTime+660, initTime+720)
eventos.append(ev)
ev = Event.Event("A","1", initTime+780, initTime+840)
eventos.append(ev)
ev = Event.Event("B","2", initTime+900, initTime+960)
eventos.append(ev)
ev = Event.Event("A","3", initTime+1020, initTime+1080)
eventos.append(ev)
ev = Event.Event("B","1", initTime+1140, initTime+1200)
eventos.append(ev)
ev = Event.Event("A","2", initTime+1260, initTime+1320)
eventos.append(ev)
ev = Event.Event("B","3", initTime+1380, initTime+1440)
eventos.append(ev)
ev = Event.Event("A","1", initTime+1500, initTime+1560)
eventos.append(ev)
ev = Event.Event("B","2", initTime+1620, initTime+1680)
eventos.append(ev)
ev = Event.Event("A","3", initTime+1740, initTime+1800)
eventos.append(ev)
ev = Event.Event("B","1", initTime+60, initTime+120)
eventos.append(ev)
ev = Event.Event("A","2", initTime+1860, initTime+1920)
eventos.append(ev)
ev = Event.Event("B","3", initTime+1980, initTime+2040)
eventos.append(ev)
ev = Event.Event("A","1", initTime+2100, initTime+2160)
eventos.append(ev)
ev = Event.Event("B","2", initTime+2220, initTime+2280)
eventos.append(ev)
ev = Event.Event("A","3", initTime+2340, initTime+2400)
eventos.append(ev)

ElasticController.indexEvent(eventos)
logger.info("Carga de los datos")



# Probar la carga de los datos en ES y modificar después la parte del .md y borrar entonces lo de java.