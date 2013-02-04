<#include "CPPHelper.ftl">
<#--#####################################################################-->
<#--                                                                   ##-->
<#--         START OF THE OUTPUT                                       ##-->
<#--                                                                   ##-->
<#--#####################################################################-->
/* \file ${file}
 * \brief Generated source file for CPP JNI interface
 * Copyright:   GBIC 2010-${year?c}, all rights reserved
 * Date:        ${date}
 * Generator:   ${generator} ${version}
 *
 * THIS FILE HAS BEEN GENERATED, PLEASE DO NOT EDIT!
 */
 
#include "${CPPName(entity)}.h"

${CPPName(entity)}::${CPPName(entity)}(JNIEnv* env)<#if entity.hasAncestor()> : ${CPPName(entity.getAncestor())}(env)</#if>{
	init(env,NULL);
}

${CPPName(entity)}::${CPPName(entity)}(JNIEnv* env, jobject obj)<#if entity.hasAncestor()> : ${CPPName(entity.getAncestor())}(env)</#if>{
	init(env,obj);
}

${CPPName(entity)}::${CPPName(entity)}(JNIEnv* env<#foreach field in entity.getImplementedFields()>, ${CPPType(field)} ${CPPName(field)}</#foreach>)<#if entity.hasAncestor()> : ${CPPName(entity.getAncestor())}(env)</#if>{
	init(env,NULL);
	<#foreach field in entity.getImplementedFields()>
	set${CPPName(field)}(${CPPName(field)});
	</#foreach>
}

void ${CPPName(entity)}::init(JNIEnv* env,jobject obj){
	this->verbose = false;
	this->env=env;
	this->clsC = env->FindClass("${entity.namespace?replace(".","/")}/${CPPName(entity)}");
	if(clsC != NULL){
    	cout << "Found: ${entity.namespace}.${CPPName(entity)}" << endl;
    	this->coID = env->GetMethodID(this->clsC, "<init>", "()V");
    	check(env,"Mapped: ${entity.namespace}.${CPPName(entity)} Constructor",verbose);
    	this->obj = env->NewObject(this->clsC, this->coID);
      	check(env,"Created: ${entity.namespace}.${CPPName(entity)}",verbose);
      	
      	this->findID = env->GetStaticMethodID(this->clsC, "find", "(Lorg/molgenis/framework/db/Database;[Lorg/molgenis/framework/db/QueryRule;)Ljava/util/List;");
    	check(env,"Mapped: ${entity.namespace}.${CPPName(entity)} QUERY",verbose);
    	this->findByNameID = env->GetStaticMethodID(this->clsC, "findByName", "(Lorg/molgenis/framework/db/Database;Ljava/lang/String;)L${entity.namespace?replace(".","/")}/${CPPName(entity)};");
    	check(env,"Mapped: ${entity.namespace}.${CPPName(entity)} findByName",verbose);
    	this->findByIdID = env->GetStaticMethodID(this->clsC, "findById", "(Lorg/molgenis/framework/db/Database;Ljava/lang/Integer;)L${entity.namespace?replace(".","/")}/${CPPName(entity)};");
    	check(env,"Mapped: ${entity.namespace}.${CPPName(entity)} findByName",verbose);
    	
    	<#foreach field in entity.getImplementedFields()>
    	this->get${CPPName(field)}ID = env->GetMethodID(this->clsC, "get${CPPName(field)}", "()${CPPJavaType(field)}");
    	check(env,"Mapped: ${entity.namespace}.${CPPName(entity)}.get${CPPName(field)}()",verbose);
    	this->set${CPPName(field)}ID = env->GetMethodID(this->clsC, "set${CPPName(field)}", "(${CPPJavaType(field)})V");
    	check(env,"Mapped: ${entity.namespace}.${CPPName(entity)}.set${CPPName(field)}(${CPPJavaType(field)})",verbose);
    	</#foreach>
    	if(obj == NULL){
    		this->obj = env->NewObject(this->clsC, this->coID);
    	}else{
    		this->obj = obj;
    	}
    	check(env,"Constructed object: ${entity.namespace}.${CPPName(entity)}",verbose);
  	}else{
  	  cout << "No such class: ${entity.namespace}.${CPPName(entity)} class" << endl;
  	}
}

jobject ${CPPName(entity)}::getJava(){
  return this->obj;
}

void ${CPPName(entity)}::check(JNIEnv* env, string message, bool verbose){
  if (env->ExceptionOccurred()) {
	env->ExceptionDescribe();
  }else{
  	if(verbose)	cout << message << endl;
  }
}

jobjectArray ${CPPName(entity)}::find(jobject db){
  jobjectArray temp =  (jobjectArray)env->CallStaticObjectMethod(this->clsC,findID, db, NULL);
  check(env,"Method called: query of ${entity.namespace}.${CPPName(entity)}",verbose);
  return temp;
}

jobject ${CPPName(entity)}::findByName(jobject db, string name){
  jobject temp =  env->CallStaticObjectMethod(this->clsC,findByNameID, db, env->NewStringUTF(name.c_str()));
  if(temp != NULL) this->obj = temp;
  check(env,"Method called: ${entity.namespace}.${CPPName(entity)}.findByName()",verbose);
  return temp;
}

jobject ${CPPName(entity)}::findById(jobject db, string id){
  jobject temp =  env->CallStaticObjectMethod(this->clsC,findByIdID, db, env->NewStringUTF(id.c_str()));
  if(temp != NULL) this->obj = temp;
  check(env,"Method called: ${entity.namespace}.${CPPName(entity)}.findById()",verbose);
  return temp;
}

${CPPName(entity)}::~${CPPName(entity)}(){
	//Compiler TODO: Figure out if I need manually to call the entity.hasAncestor
}


<#foreach field in entity.getImplementedFields()>
  	
${CPPType(field)} ${CPPName(entity)}::get${CPPName(field)}(void){
	${CPPType(field)} r;
	<#if ( CPPType(field) == "string") >
	jboolean blnIsCopy;
	r = env->GetStringUTFChars((jstring)env->CallObjectMethod(this->obj,get${CPPName(field)}ID),&blnIsCopy);
	<#elseif (CPPType(field) == "bool")>
	r = (${CPPType(field)})env->CallBooleanMethod(this->obj,get${CPPName(field)}ID);
	<#elseif (CPPType(field) == "double")>
	r = (${CPPType(field)})env->CallDoubleMethod(this->obj,get${CPPName(field)}ID);
	<#elseif (CPPType(field) == "int")>
	r = (${CPPType(field)})env->CallIntMethod(this->obj,get${CPPName(field)}ID);
	<#elseif (CPPType(field) == "vector<int>")>
	jintArray javaArray = (jintArray)env->CallObjectMethod(this->obj,get${CPPName(field)}ID);
	jint nitems = env->GetArrayLength(javaArray);
	cout << "Found:" << nitems << "in array" << endl;
	for(int x=0;x<nitems;x++){
	  r.push_back((int)(*env->GetIntArrayElements(javaArray, 0)));
	}
	<#else>
	r = (${CPPType(field)})env->CallObjectMethod(this->obj,get${CPPName(field)}ID);
	</#if>
	return r;
}

void ${CPPName(entity)}::set${CPPName(field)}(${CPPType(field)} in){
	env->CallObjectMethod(this->obj,set${CPPName(field)}ID);
}
</#foreach>