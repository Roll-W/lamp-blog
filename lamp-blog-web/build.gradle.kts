/*
 * Copyright (C) 2023 RollW
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("buildlogic.java-conventions")
}

dependencies {
    implementation(project(":lamp-blog-common"))
    implementation(project(":lamp-blog-content:content-service"))
    implementation(project(":lamp-blog-user-service"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.rollw.web.common.spring.boot.starter)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.apache.commons:commons-lang3")
    implementation(libs.com.google.guava)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation(libs.caffeine)
    implementation("com.mysql:mysql-connector-j")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation(libs.io.jsonwebtoken.jjwt.api)
    implementation(libs.io.jsonwebtoken.jjwt.impl)
    implementation(libs.io.jsonwebtoken.jjwt.jackson)
    implementation(libs.com.zaxxer.hikaricp)
    implementation(libs.bundles.light)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    annotationProcessor(libs.lingu.light.compiler)
}

description = "lamp-blog-web"
