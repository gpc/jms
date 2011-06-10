/*
 * Copyright 2010 Grails Plugin Collective
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.jms.bean

class MapBasedBeanDefinitionBuilder {

    private static final String BEAN_QUALIFIER = 'Bean'

    private name
    private definition

    MapBasedBeanDefinitionBuilder(name, Map definition) {
        this.name = name
        this.definition = definition
    }

    def getName() {
        name
    }
    
    def getClazz() {
        definition.clazz
    }
    
    def getMeta() {
        definition.meta
    }    

    def getProperties() {
        def properties = definition.clone()
        properties.remove('clazz')
        properties.remove('meta')
        properties
    }

    def build(beanBuilder) {
        beanBuilder.with {
            "${this.getName()}"(clazz) { metaBean ->
                def bean = delegate

                this.meta.each { k, v ->
                    this.set(k, v, metaBean, beanBuilder)
                }
                this.properties.each { k, v ->
                    this.set(k, v, bean, beanBuilder)
                }
            }
        }
    }

    def removeFrom(context) {
        def beanName = getName()
        context.containsBean(beanName) && context.removeBeanDefinition(beanName)
    }

    /**
     * Will bind a property to the given recipient. If such property qualifies as a
     * <i>Spring Bean</i>, it's name has the {@link MapBasedBeanDefinitionBuilder#BEAN_QUALIFIER} as suffix,
     * it will assign the bean named as {@code value} if such {@code value} is not {@code null} (i.e. {@code value ? ref(value) : null } ).
     * If such name doesn't qualify as a bean the given value will be assigned directly to the attribute named as {@code name}.
     * @param name Name of the attribute, if suffixed by {@link MapBasedBeanDefinitionBuilder#BEAN_QUALIFIER} it will reference a Spring Bean.
     * @param value direct value or name of the bean in the Application Context
     * @param recipient
     * @param beanBuilder
     * @return
     */
    protected set(name, value, recipient, beanBuilder) {
        if (name.endsWith(BEAN_QUALIFIER)) {
            recipient."${name.substring(0, name.size() - BEAN_QUALIFIER.size())}" = value ? beanBuilder.ref(value) : null
        } else {
            recipient."$name" = value
        }
    }
}
