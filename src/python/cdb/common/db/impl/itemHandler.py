#!/usr/bin/env python

from sqlalchemy import and_
from sqlalchemy.orm.exc import NoResultFound

from cdb.common.exceptions.objectAlreadyExists import ObjectAlreadyExists
from cdb.common.exceptions.objectNotFound import ObjectNotFound
from cdb.common.exceptions.invalidArgument import InvalidArgument
from cdb.common.db.impl.entityInfoHandler import EntityInfoHandler
from cdb.common.db.impl.entityTypeHandler import EntityTypeHandler
from cdb.common.db.entities.item import Item
from cdb.common.db.entities.itemElement import ItemElement
from cdb.common.db.entities.itemEntityType import ItemEntityType
from cdb.common.db.entities.itemSource import ItemSource
from cdb.common.db.entities.itemCategory import ItemCategory
from cdb.common.db.entities.itemType import ItemType
from cdb.common.db.entities.itemItemCategory import ItemItemCategory
from cdb.common.db.entities.itemItemType import ItemItemType
from cdb.common.db.entities.itemLog import ItemLog
from cdb.common.db.entities.itemElementProperty import ItemElementProperty
from cdb.common.db.entities.itemElementRelationship import ItemElementRelationship
from cdb.common.db.entities.itemConnector import ItemConnector
from cdb.common.db.entities.itemElementLog import ItemElementLog
from cdb.common.db.entities.itemElementRelationshipHistory import ItemElementRelationshipHistory
from cdb.common.db.impl.cdbDbEntityHandler import CdbDbEntityHandler
from cdb.common.db.impl.domainHandler import DomainHandler
from cdb.common.db.impl.sourceHandler import SourceHandler
from cdb.common.db.impl.logHandler import LogHandler
from cdb.common.db.impl.propertyValueHandler import PropertyValueHandler
from cdb.common.db.impl.relationshipTypeHandler import RelationshipTypeHandler
from cdb.common.db.impl.resourceTypeHandler import ResourceTypeHandler
from cdb.common.db.impl.userInfoHandler import UserInfoHandler


class ItemHandler(CdbDbEntityHandler):

    def __init__(self):
        CdbDbEntityHandler.__init__(self)
        self.entityInfoHandler = EntityInfoHandler()
        self.domainHandler = DomainHandler()
        self.entityTypeHandler = EntityTypeHandler()
        self.sourceHandler = SourceHandler()
        self.logHandler = LogHandler()
        self.propertyValueHandler = PropertyValueHandler()
        self.relationshipTypeHandler = RelationshipTypeHandler()
        self.resourceTypeHandler = ResourceTypeHandler()
        self.userInfoHandler = UserInfoHandler()

    def getItemById(self, session, id):
        return self._findDbObjById(session, Item, id)

    def getItemElementsByName(self, session, name):
        return self._findDbObjByName(session, ItemElement, name)

    def getItemElementById(self, session, id):
        return self._findDbObjById(session, ItemElement, id)

    def addItemCategory(self, session, itemCategoryName, description):
        return self._addSimpleNameDescriptionTable(session, ItemCategory, itemCategoryName, description)

    def addItemType(self, session, itemTypeName, description):
        return self._addSimpleNameDescriptionTable(session, ItemType, itemTypeName, description)

    def getItemCategoryByName(self, session, name):
        return self._findDbObjByName(session, ItemCategory, name)

    def getItemTypeByName(self, session, name):
        return self._findDbObjByName(session, ItemType, name)

    def getItemConnectorById(self, session, id):
        return self._findDbObjById(session, ItemConnector, id)

    def getItemElementRelationshipById(self, session, id):
        return self._findDbObjById(session, ItemElementRelationship, id)

    def getItemByUniqueAttributes(self, session, domainId, name, itemIdentifier1, itemIdentifier2):
        entityDisplayName = self._getEntityDisplayName(Item)
        try:
            dbItem = session.query(Item).filter(Item.domain_id==domainId)\
                .filter(Item.name==name)\
                .filter(Item.item_identifier1==itemIdentifier1)\
                .filter(Item.item_identifier2==itemIdentifier2).one()
            return dbItem
        except NoResultFound, ex:
            raise ObjectNotFound('No %s with name: %s, item identifier 1: %s, item identifier 2: %s in domain id %s exists.'
                                 % (entityDisplayName, name, itemIdentifier1, itemIdentifier1, domainId))

    def addItem(self, session, domainName, name, derivedFromItemId, itemIdentifier1, itemIdentifier2, entityTypeName, qrId, description,
                    createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, createdOnDataTime=None, lastModifiedOnDateTime=None):

        dbItems = session.query(Item).filter(Item.name==name).all()
        for item in dbItems:
            if item.item_identifier1 == itemIdentifier1 \
                        and item.item_identifier2 == itemIdentifier2 \
                        and item.domain.name == domainName:
                raise ObjectAlreadyExists('Item with name %s already exists.' % name)


        # Create entity info
        entityInfoArgs = (createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, createdOnDataTime, lastModifiedOnDateTime)
        dbEntityInfo = self.entityInfoHandler.createEntityInfo(session, *entityInfoArgs)

        # Create item
        dbItem = Item(name=name)
        dbItem.entityInfo = dbEntityInfo
        dbItem.domain = self.domainHandler.findDomainByName(session, domainName)
        dbItem.derived_from_item_id = derivedFromItemId
        dbItem.qr_id = qrId
        dbItem.description=description
        dbItem.item_identifier1 = itemIdentifier1
        dbItem.item_identifier2 = itemIdentifier2

        session.add(dbItem)
        session.flush()

        # Add self element
        self.addItemElement(session, None, dbItem.id, None, False, None, *entityInfoArgs)

        self.logger.debug('Inserted item id %s' % dbItem.id)
        self.addItemEntityType(session, None, entityTypeName, dbItem)

        return dbItem

    def getSelfElementByItemId(self, session, itemId):
        entityDisplayName = self._getEntityDisplayName(ItemElement)
        try:
            dbItemElement = session.query(ItemElement).filter(ItemElement.parent_item_id==itemId).filter(ItemElement.name==None).one()
            return dbItemElement
        except NoResultFound, ex:
            raise ObjectNotFound('No %s with id %s exists.' % (entityDisplayName, id))

    def addItemEntityType(self, session, itemId, entityTypeName, item=None):
        dbItemEntityType = ItemEntityType()

        if item:
            dbItemEntityType.item = item
        else:
            dbItemEntityType.item = self.getItemById(session, itemId)

        dbItemEntityType.entityType = self.entityTypeHandler.findEntityTypeByName(session, entityTypeName)

        session.add(dbItemEntityType)
        session.flush()

        self.logger.debug('Inserted Item Entity Type for item id %s' % dbItemEntityType.item.id)

        return dbItemEntityType

    def addItemElement(self, session, name, parentItemId, containedItemId, isRequired, description,
                createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable , createdOnDataTime=None, lastModifiedOnDateTime=None):
        dbItemElements = session.query(ItemElement).filter(ItemElement.name==name).all()
        for element in dbItemElements:
            if element.parent_item_id == parentItemId and element.name == name:
                raise ObjectAlreadyExists('Item with name %s already exists.' % name)

        # Create entity info
        dbEntityInfo = self.entityInfoHandler.createEntityInfo(session, createdByUserId, ownerUserId, ownerGroupId, isGroupWriteable, createdOnDataTime, lastModifiedOnDateTime)

        # Create item
        dbItemElement = ItemElement(name=name, description=description)
        dbItemElement.entityInfo = dbEntityInfo
        if parentItemId:
            dbItemElement.parentItem = self.getItemById(session, parentItemId)
        if containedItemId:
            dbItemElement.containedItem = self.getItemById(session, containedItemId)

        session.add(dbItemElement)
        session.flush()
        self.logger.debug('Inserted item Element id %s' % dbItemElement.id)

        return dbItemElement

    def addItemSource(self, session, itemId, sourceName, partNumber, cost, description, isVendor, isManufacturer, contactInfo, url):
        dbItem = self.getItemById(session, itemId)
        dbSource = self.sourceHandler.findSourceByName(session, sourceName)

        dbItemSource = ItemSource()
        dbItemSource.item = dbItem
        dbItemSource.source = dbSource
        if partNumber:
            dbItemSource.part_number = partNumber
        if cost:
            dbItemSource.cost = cost
        if description:
            dbItemSource.description = description
        if isVendor:
            dbItemSource.is_vendor = isVendor
        if isManufacturer:
            dbItemSource.is_manufacturer = isManufacturer
        if contactInfo:
            dbItemSource.contact_info = contactInfo
        if url:
            dbItemSource.url = url

        session.add(dbItemSource)
        session.flush()
        self.logger.debug('Inserted item source %s' % dbItemSource.id)

        return dbItemSource

    def addItemItemCategory(self, session, itemId, itemCategoryName):
        dbItem = self.getItemById(session, itemId)
        dbCategory = self.getItemCategoryByName(session, itemCategoryName)

        dbItemItemCategory = ItemItemCategory()
        dbItemItemCategory.item = dbItem
        dbItemItemCategory.category = dbCategory

        session.add(dbItemItemCategory)
        session.flush()

        self.logger.debug('Added category %s for item id %s' % (itemCategoryName, itemId))

        return dbItemItemCategory

    def addItemItemType(self, session, itemId, itemTypeName):
        dbItem = self.getItemById(session, itemId)
        dbType = self.getItemTypeByName(session, itemTypeName)

        dbItemItemType = ItemItemType()
        dbItemItemType.item = dbItem
        dbItemItemType.type = dbType

        session.add(dbItemItemType)
        
        session.flush()
        self.logger.debug('Added type %s for item id %s' % (itemTypeName, itemId))
        return dbItemItemType

    def addItemLog(self, session, itemId, text, enteredByUserId, effectiveFromDateTime, effectiveToDateTime, logTopicName, enteredOnDateTime = None):
        dbItem = self.getItemById(session, itemId)
        dbLog = self.logHandler.addLog(session, text, enteredByUserId, effectiveFromDateTime, effectiveToDateTime, logTopicName, enteredOnDateTime)

        dbItemLog = ItemLog()
        dbItemLog.item = dbItem
        dbItemLog.log = dbLog

        session.add(dbItemLog)
        session.flush()

        self.logger.debug('Added log for item id %s' % (itemId))
        return dbItemLog

    def addItemElementLog(self, session, itemElementId, text, enteredByUserId, effectiveFromDateTime, effectiveToDateTime, logTopicName, enteredOnDateTime = None):
        dbItemElement = self.getItemElementById(session, itemElementId)
        dbLog = self.logHandler.addLog(session, text, enteredByUserId, effectiveFromDateTime, effectiveToDateTime, logTopicName, enteredOnDateTime)

        dbItemElementLog = ItemElementLog()
        dbItemElementLog.itemElement = dbItemElement
        dbItemElementLog.log = dbLog

        session.add(dbItemElementLog)
        session.flush()

        self.logger.debug('Added log for itemElement id %s' % (itemElementId))
        return dbItemElementLog

    def addItemElementProperty(self, session, itemElementId, propertyTypeName, tag, value, units, description, enteredByUserId, isUserWriteable, isDynamic, displayValue, targetValue, enteredOnDateTime = None):
        dbItemElement = self.getItemElementById(session, itemElementId)
        dbPropertyValue = self.propertyValueHandler.createPropertyValue(session, propertyTypeName, tag, value, units, description, enteredByUserId, isUserWriteable, isDynamic, displayValue,targetValue, enteredOnDateTime)

        session.add(dbPropertyValue)

        dbItemElementProperty = ItemElementProperty()
        dbItemElementProperty.itemElement = dbItemElement
        dbItemElementProperty.propertyValue = dbPropertyValue

        session.add(dbItemElementProperty)

        session.flush()

        self.logger.debug('Added property value (type: %s) for item element id %s' % (propertyTypeName, itemElementId))
        return dbItemElementProperty

    def addItemElementRelationship(self, session, firstItemElementId, secondItemElementId, firstItemConnectorId, secondItemConnectorId,
                                   linkItemElementId, relationshipTypeName, relationshipDetails, resourceTypeName, label, description ):
        firstItemElement = self.getItemElementById(session, firstItemElementId)
        secondItemElement = self.getItemElementById(session, secondItemElementId)
        relationshipType = self.relationshipTypeHandler.getRelationshipTypeByName(session, relationshipTypeName)

        dbItemElementRelationship = ItemElementRelationship()
        dbItemElementRelationship.firstItemElement = firstItemElement
        dbItemElementRelationship.secondItemElement = secondItemElement
        dbItemElementRelationship.relationshipType = relationshipType
        dbItemElementRelationship.relationship_details = relationshipDetails
        dbItemElementRelationship.label = label
        dbItemElementRelationship.description = description

        if firstItemConnectorId:
            dbItemElementRelationship.firstItemConnector = self.getItemConnectorById(firstItemConnectorId)
        if secondItemConnectorId:
            dbItemElementRelationship.secondItemConnector = self.getItemConnectorById(secondItemConnectorId)
        if linkItemElementId:
            dbItemElementRelationship.linkItemElement = self.getItemElementById(linkItemElementId)
        if resourceTypeName:
            dbItemElementRelationship.resourceType = self.resourceTypeHandler.getResourceTypeByName(session, resourceTypeName)

        session.add(dbItemElementRelationship)
        session.flush()

        entityDisplayName = self._getEntityDisplayName(ItemElementRelationship)
        self.logger.debug('Inserted %s %s' % (entityDisplayName, dbItemElementRelationship.id))

        return dbItemElementRelationship

    def addItemElementRelationshipHistory(self, session, itemRelationshipId, firstItemElementId, secondItemElementId, firstItemConnectorId, secondItemConnectorId,
                                   linkItemElementId, relationshipDetails, resourceTypeName, label, description, enteredByUserId, enteredOnDateTime ):
        firstItemElement = self.getItemElementById(session, firstItemElementId)
        secondItemElement = self.getItemElementById(session, secondItemElementId)
        dbItemElementRelationship = self.getItemElementRelationshipById(session, itemRelationshipId)

        dbItemElementRelationshipHistory = ItemElementRelationshipHistory()
        dbItemElementRelationshipHistory.firstItemElement = firstItemElement
        dbItemElementRelationshipHistory.secondItemElement = secondItemElement
        dbItemElementRelationshipHistory.itemElementRelationship = dbItemElementRelationship
        dbItemElementRelationshipHistory.relationship_details = relationshipDetails
        dbItemElementRelationshipHistory.label = label
        dbItemElementRelationshipHistory.description = description
        dbItemElementRelationshipHistory.enteredByUserInfo = self.userInfoHandler.getUserInfoById(session, enteredByUserId)
        dbItemElementRelationshipHistory.entered_on_date_time = enteredOnDateTime

        if firstItemConnectorId:
            dbItemElementRelationshipHistory.firstItemConnector = self.getItemConnectorById(firstItemConnectorId)
        if secondItemConnectorId:
            dbItemElementRelationshipHistory.secondItemConnector = self.getItemConnectorById(secondItemConnectorId)
        if linkItemElementId:
            dbItemElementRelationshipHistory.linkItemElement = self.getItemElementById(linkItemElementId)
        if resourceTypeName:
            dbItemElementRelationshipHistory.resourceType = self.resourceTypeHandler.getResourceTypeByName(session, resourceTypeName)

        session.add(dbItemElementRelationshipHistory)
        session.flush()

        entityDisplayName = self._getEntityDisplayName(ItemElementRelationshipHistory)
        self.logger.debug('Inserted %s %s' % (entityDisplayName, dbItemElementRelationshipHistory.id))

        return dbItemElementRelationshipHistory






