/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com) and
 * JR Boyens <gnu-jrb[remove] at gmx dot net>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.querymanagers.generic;

import rife.database.querymanagers.generic.exceptions.*;

import java.util.*;

import rife.database.exceptions.DatabaseException;
import rife.tools.BeanPropertyProcessor;
import rife.tools.BeanUtils;
import rife.tools.ClassUtils;
import rife.tools.JavaSpecificationUtils;
import rife.tools.exceptions.BeanUtilsException;
import rife.validation.Constrained;
import rife.validation.ConstrainedProperty;
import rife.validation.ConstrainedUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class to provide many-to-many and many-to-one relational
 * capabilities to generic query manager implementations.
 *
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.0
 */
public abstract class GenericQueryManagerRelationalUtils {
    /**
     * Restores a constrained many-to-one property value that is lazily loaded.
     *
     * @param manager               the {@code GenericQueryManager} that will be used to
     *                              restore the related bean instance
     * @param constrained           the constrained bean instance that contains the
     *                              property whose value will be restored
     * @param propertyName          the name of the property value that will be restored
     * @param propertyTypeClassName the class name of the property that will be
     *                              restored
     * @return the value of the property, or
     * <p>{@code null} if the constrained property doesn't exist or if it
     * didn't have the {@code manyToOne} constraint
     * @since 1.0
     */
    public static Object restoreLazyManyToOneProperty(GenericQueryManager manager, Constrained constrained, String propertyName, String propertyTypeClassName) {
        Object result = null;

        var property = constrained.getConstrainedProperty(propertyName);

        // only lazily load the property value if a constrained property has been found
        if (property != null) {
            // only consider a constrained property with a many-to-one constraint
            if (property.hasManyToOne()) {
                // try to obtain the class for the property's type
                Class return_type = null;
                try {
                    return_type = Class.forName(propertyTypeClassName);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                // obtain the associated class from the many-to-one constraint
                var associated_class = property.getManyToOne().getAssociatedClass();

                // if the associated class wasn't specified, use the property's type
                if (null == associated_class) {
                    associated_class = return_type;
                } else {
                    // ensure that the specified associated class is compatible with the property's type
                    if (!return_type.isAssignableFrom(associated_class)) {
                        throw new IncompatibleManyToOneValueTypeException(manager.getBaseClass(), propertyName, return_type, associated_class);
                    }
                }

                // retrieve the entity from the database for this property
                var declaration = createManyToOneDeclaration(manager, property, return_type);
                if (!declaration.isBasic()) {
                    var column_name = generateManyToOneJoinColumnName(property.getPropertyName(), declaration);
                    result = restoreManyToOneProperty(manager, constrained, declaration.getAssociationManager(), column_name, associated_class);
                }
            }
        }

        return result;
    }

    public static Object restoreManyToOneProperty(GenericQueryManager manager, Object constrained, GenericQueryManager associationManager, String columnName, Class propertyType) {
        var query = associationManager.getRestoreQuery()
            .fields(associationManager.getTable(), propertyType)
            .join(manager.getTable())
            .where(associationManager.getTable() + "." + associationManager.getIdentifierName() + " = " + manager.getTable() + "." + columnName)
            .whereAnd(manager.getTable() + "." + manager.getIdentifierName(), "=", manager.getIdentifierValue(constrained));
        return associationManager.restoreFirst(query);
    }

    public static ManyToOneDeclaration createManyToOneDeclaration(GenericQueryManager manager, ConstrainedProperty property, Class propertyType)
    throws IncompatibleManyToOneValueTypeException {
        ManyToOneDeclaration declaration = null;

        if (property != null &&
            property.hasManyToOne()) {
            var many_to_one = property.getManyToOne();

            declaration = new ManyToOneDeclaration()
                .associationType(many_to_one.getAssociatedClass())
                .associationColumn(many_to_one.getColumn());

            // fall back to the associated class in case the property type wasn't provided
            if (null == propertyType) {
                propertyType = declaration.getAssociationType();
            }

            // detect whether the property is a basic type, or if its type is unknown
            // it will be treated as a primary key and not an object instance of data that's
            // stored in the associated table
            if (null == propertyType ||
                ClassUtils.isBasic(propertyType)) {
                declaration
                    .isBasic(true)
                    .associationTable(many_to_one.getDerivedTable());
            } else {
                declaration
                    .isBasic(false)
                    .associationTable(many_to_one.getTable());

                // retrieve the class that has been associated to the property through constraints
                var associated_class = declaration.getAssociationType();

                // if an associated class has already been specified through constraints,
                // ensure that it's compatible with the type of the property
                if (associated_class != null) {
                    if (!propertyType.isAssignableFrom(associated_class)) {
                        throw new IncompatibleManyToOneValueTypeException(manager.getBaseClass(), property.getName(), propertyType, associated_class);
                    }
                }
                // since no associated class has been specified yet, use the property type
                else {
                    declaration.setAssociationType(propertyType);
                }

                // create the association query manager
                var association_manager = manager.createNewManager(declaration.getAssociationType());
                declaration.setAssociationManager(association_manager);

                // determine the association table
                if (null == declaration.getAssociationTable()) {
                    declaration.setAssociationTable(association_manager.getTable());
                }

                // determine the association column name
                if (null == declaration.getAssociationColumn()) {
                    declaration.setAssociationColumn(association_manager.getIdentifierName());
                }
            }
        }

        return declaration;
    }

    public static Map<String, ManyToOneDeclaration> obtainManyToOneDeclarations(final GenericQueryManager manager, final Constrained constrained, final String fixedMainProperty, final Class fixedAssocationType) {
        final Map<String, ManyToOneDeclaration> declarations;
        if (constrained != null &&
            constrained.hasPropertyConstraint(ConstrainedProperty.MANY_TO_ONE)) {
            declarations = new LinkedHashMap<>();

            // collect all properties that have a many-to-one relationship
            final List<String> property_names = new ArrayList<>();
            if (fixedMainProperty != null) {
                var property = constrained.getConstrainedProperty(fixedMainProperty);
                if (property.hasManyToOne()) {
                    property_names.add(property.getPropertyName());
                }
            } else {
                for (var property : (Collection<ConstrainedProperty>) constrained.getConstrainedProperties()) {
                    if (property.hasManyToOne()) {
                        property_names.add(property.getPropertyName());
                    }
                }
            }

            // obtain the actual bean properties for the many-to-one relationships
            if (property_names.size() > 0) {
                var unresolved_name_array = new String[property_names.size()];
                property_names.toArray(unresolved_name_array);
                try {
                    BeanUtils.processProperties(manager.getBaseClass(), unresolved_name_array, null, null, (name, descriptor) -> {
                        var property = constrained.getConstrainedProperty(name);
                        var declaration = createManyToOneDeclaration(manager, property, descriptor.getReadMethod().getReturnType());
                        if (declaration != null) {
                            if (null == fixedAssocationType ||
                                fixedAssocationType == declaration.getAssociationType()) {
                                declarations.put(property.getPropertyName(), declaration);

                                if (fixedAssocationType != null) {
                                    return false;
                                }
                            }
                        }

                        return true;
                    });
                } catch (BeanUtilsException e) {
                    throw new DatabaseException(e);
                }
            }
        } else {
            declarations = null;
        }

        return declarations;
    }

    public static Map<String, ManyToManyDeclaration> obtainManyToManyDeclarations(final GenericQueryManager manager, Constrained constrained, boolean includeAssociations) {
        final Map<String, ManyToManyDeclaration> declarations;
        if (constrained != null &&
            (constrained.hasPropertyConstraint(ConstrainedProperty.MANY_TO_MANY) ||
             includeAssociations && constrained.hasPropertyConstraint(ConstrainedProperty.MANY_TO_MANY_ASSOCIATION))) {
            declarations = new HashMap<>();

            // collect all properties that have a many-to-many relationship
            final List<String> unresolved_name_list = new ArrayList<String>();
            for (var property : constrained.getConstrainedProperties()) {
                if (property.hasManyToMany()) {
                    declarations.put(property.getPropertyName(), new ManyToManyDeclaration()
                        .associationType(property.getManyToMany().getAssociatedClass()));
                    unresolved_name_list.add(property.getPropertyName());
                } else if (includeAssociations && property.hasManyToManyAssociation()) {
                    declarations.put(property.getPropertyName(), new ManyToManyDeclaration()
                        .reversed(true)
                        .associationType(property.getManyToManyAssociation().getAssociatedClass()));
                    unresolved_name_list.add(property.getPropertyName());
                }
            }

            // obtain the actual bean properties for the many-to-many relationships
            if (unresolved_name_list.size() > 0) {
                var unresolved_name_array = new String[unresolved_name_list.size()];
                unresolved_name_list.toArray(unresolved_name_array);
                try {
                    BeanUtils.processProperties(manager.getBaseClass(), unresolved_name_array, null, null, (name, descriptor) -> {
                        var read_method = descriptor.getReadMethod();

                        var declaration = declarations.get(name);

                        // make sure that the many-to-many property has a supported collection type
                        Class return_type = read_method.getReturnType();
                        ensureSupportedManyToManyPropertyCollectionType(manager.getBaseClass(), name, return_type);
                        declaration.setCollectionType(return_type);

                        // check if the class of the relationship has already been set, otherwise detect it from
                        // the generic information that's available in the collection
                        if (null == declaration.getAssociationType()) {
                            Class associated_class = null;
                            try {
                                Class klass = Class.forName("rife.database.querymanagers.generic.GenericTypeDetector");
                                var method = klass.getDeclaredMethod("detectAssociatedClass", Method.class);
                                associated_class = (Class) method.invoke(null, read_method);
                            } catch (Exception e) {
                                throw new DatabaseException(e);
                            }

                            if (null == associated_class) {
                                throw new MissingManyToManyTypeInformationException(manager.getBaseClass(), name);
                            }

                            declaration.setAssociationType(associated_class);
                        }

                        return false;
                    });
                } catch (BeanUtilsException e) {
                    throw new DatabaseException(e);
                }
            }
        } else {
            declarations = null;
        }

        return declarations;
    }

    public static Map<String, ManyToOneAssociationDeclaration> obtainManyToOneAssociationDeclarations(final GenericQueryManager manager, Constrained constrained) {
        final Map<String, ManyToOneAssociationDeclaration> declarations;
        if (constrained != null &&
            constrained.hasPropertyConstraint(ConstrainedProperty.MANY_TO_ONE_ASSOCIATION)) {
            declarations = new HashMap<>();

            // collect all properties that have a many-to-one association relationship
            final List<String> unresolved_name_list = new ArrayList<>();
            for (var property : constrained.getConstrainedProperties()) {
                if (property.hasManyToOneAssociation()) {
                    var association = property.getManyToOneAssociation();
                    declarations.put(property.getPropertyName(), new ManyToOneAssociationDeclaration()
                        .mainType(association.getMainClass())
                        .mainProperty(association.getMainProperty()));
                    unresolved_name_list.add(property.getPropertyName());
                }
            }

            // obtain the actual bean properties for the many-to-one association relationships
            if (unresolved_name_list.size() > 0) {
                var unresolved_name_array = new String[unresolved_name_list.size()];
                unresolved_name_list.toArray(unresolved_name_array);
                try {
                    BeanUtils.processProperties(manager.getBaseClass(), unresolved_name_array, null, null, (name, descriptor) -> {
                        var read_method = descriptor.getReadMethod();

                        var declaration = declarations.get(name);

                        // make sure that the many-to-one association property has a supported collection type
                        Class return_type = read_method.getReturnType();
                        ensureSupportedManyToOneAssociationPropertyCollectionType(manager.getBaseClass(), name, return_type);
                        declaration.setCollectionType(return_type);

                        // check if the class of the relationship has already been set, otherwise detect it from
                        // the generic information that's available in the collection
                        if (null == declaration.getMainType()) {
                            Class associated_class = null;
                            try {
                                Class klass = Class.forName("rife.database.querymanagers.generic.GenericTypeDetector");
                                var method = klass.getDeclaredMethod("detectAssociatedClass", Method.class);
                                associated_class = (Class) method.invoke(null, read_method);
                            } catch (Exception e) {
                                throw new DatabaseException(e);
                            }

                            if (null == associated_class) {
                                throw new MissingManyToOneAssociationTypeInformationException(manager.getBaseClass(), name);
                            }

                            declaration.setMainType(associated_class);
                        }

                        // obtain the main declaration
                        var main_constrained = ConstrainedUtils.getConstrainedInstance(declaration.getMainType());
                        var main_manager = manager.createNewManager(declaration.getMainType());
                        var main_declarations = obtainManyToOneDeclarations(main_manager, main_constrained, declaration.getMainProperty(), manager.getBaseClass());
                        if (null == main_declarations ||
                            0 == main_declarations.size()) {
                            throw new MissingManyToOneMainPropertyException(manager.getBaseClass(), name, declaration.getMainType());
                        } else {
                            var main_entry = main_declarations.entrySet().iterator().next();
                            declaration
                                .mainProperty(main_entry.getKey())
                                .mainDeclaration(main_entry.getValue());
                        }

                        return false;
                    });
                } catch (BeanUtilsException e) {
                    throw new DatabaseException(e);
                }
            }
        } else {
            declarations = null;
        }

        return declarations;
    }

    public static String generateManyToManyJoinTableName(ManyToManyDeclaration association, GenericQueryManager manager1, GenericQueryManager manager2) {
        if (association.isReversed()) {
            return manager2.getTable() + "_" + manager1.getTable();
        } else {
            return manager1.getTable() + "_" + manager2.getTable();
        }
    }

    public static String generateManyToManyJoinColumnName(GenericQueryManager manager) {
        return manager.getTable() + "_" + manager.getIdentifierName();
    }

    public static String generateManyToOneJoinColumnName(String mainPropertyName, ManyToOneDeclaration declaration) {
        return mainPropertyName + "_" + declaration.getAssociationColumn();
    }

    public static void processManyToOneJoinColumns(GenericQueryManager manager, ManyToOneJoinColumnProcessor processor) {
        if (null == processor) {
            return;
        }

        // generate many-to-one join columns
        var constrained = ConstrainedUtils.getConstrainedInstance(manager.getBaseClass());
        if (constrained != null &&
            constrained.hasPropertyConstraint(ConstrainedProperty.MANY_TO_ONE)) {
            var manytoone_declarations = obtainManyToOneDeclarations(manager, constrained, null, null);
            if (manytoone_declarations != null) {
                // iterate over all the many-to-one relationships that have associated classes
                for (var entry : manytoone_declarations.entrySet()) {
                    var declaration = entry.getValue();
                    if (!declaration.isBasic()) {
                        var column_name = generateManyToOneJoinColumnName(entry.getKey(), declaration);
                        if (!processor.processJoinColumn(column_name, entry.getKey(), declaration)) {
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void ensureSupportedManyToManyPropertyCollectionType(Class beanClass, String propertyName, Class propertyType)
    throws UnsupportedManyToManyPropertyTypeException {
        if (!(propertyType == Collection.class ||
              propertyType == Set.class ||
              propertyType == List.class)) {
            throw new UnsupportedManyToManyPropertyTypeException(beanClass, propertyName, propertyType);
        }
    }

    public static void ensureSupportedManyToManyPropertyValueType(Class beanClass, String propertyName, Object propertyValue)
    throws UnsupportedManyToManyPropertyTypeException {
        if (!(propertyValue instanceof Collection)) {
            throw new UnsupportedManyToManyValueTypeException(beanClass, propertyName, propertyValue);
        }
    }

    public static void ensureSupportedManyToOneAssociationPropertyCollectionType(Class beanClass, String propertyName, Class propertyType)
    throws UnsupportedManyToManyPropertyTypeException {
        if (!(propertyType == Collection.class ||
              propertyType == Set.class ||
              propertyType == List.class)) {
            throw new UnsupportedManyToOneAssociationPropertyTypeException(beanClass, propertyName, propertyType);
        }
    }

    public static void ensureSupportedManyToOneAssociationPropertyValueType(Class beanClass, String propertyName, Object propertyValue)
    throws UnsupportedManyToManyPropertyTypeException {
        if (!(propertyValue instanceof Collection)) {
            throw new UnsupportedManyToOneValueTypeException(beanClass, propertyName, propertyValue);
        }
    }
}
