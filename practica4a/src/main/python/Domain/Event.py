""" Clase Event

Define los atributos y métodos para la clase "Event"

@date: 02/11/2020
@version: 1.1.0
@author: Antonio
@status: Development
"""


class Event(object):
    # Campos de event en Elasticsearch
    EVENT_ID = "Event_ID"
    EVENT_TAG = "Tag"
    EVENT_T1 = "T1"
    EVENT_T2 = "T2"

    def __init__(self, Event_ID, Tag, T1, T2):
        """ Constructor de la clase
        """
        self.Event_ID = Event_ID
        self.Tag = Tag
        self.T1 = T1
        self.T2 = T2
