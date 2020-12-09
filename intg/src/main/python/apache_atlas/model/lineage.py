#!/usr/bin/env/python

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from apache_atlas.model.instance import *
from apache_atlas.utils          import *


class AtlasLineageInfo(AtlasBase):
    def __init__(self, attrs={}):
        AtlasBase.__init__(self, attrs)

        self.baseEntityGuid   = attrs.get('baseEntityGuid')
        self.lineageDirection = attrs.get('lineageDirection')
        self.lineageDepth     = attrs.get('lineageDepth')
        self.guidEntityMap    = attrs.get('guidEntityMap')
        self.relations        = attrs.get('relations')


    def type_coerce_attrs(self):
        super(AtlasLineageInfo, self).type_coerce_attrs()

        self.guidEntityMap = type_coerce_dict(self.guidEntityMap, AtlasEntityHeader)
        self.relations     = type_coerce_list(self.relations, LineageRelation)


class LineageRelation(AtlasBase):
    def __init__(self, attrs):
        AtlasBase.__init__(self, attrs)

        self.fromEntityId   = attrs.get('fromEntityId')
        self.toEntityId     = attrs.get('toEntityId')
        self.relationshipId = attrs.get('relationshipId')
