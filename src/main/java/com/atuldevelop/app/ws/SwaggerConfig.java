package com.atuldevelop.app.ws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	List<VendorExtension> vendorExtensions = new ArrayList<>();
	
	ApiInfo apiInfo = new ApiInfo(
			"Photo app RESTful Web Service documentation",
			"This pages documents Photo app RESTful Web Service endpoints", 
			"1.0",
			"http://www.appsdeveloperblog.com/service.html", 
			null, 
			"Apache 2.0",
			"http://www.apache.org/licenses/LICENSE-2.0", 
			vendorExtensions);

	@Bean
	public Docket apiDocket() {

		Docket docket = new Docket(DocumentationType.OAS_30)
				.protocols(new HashSet<>(Arrays.asList("HTTP","HTTPs")))
				.apiInfo(apiInfo)
				.select()
				.apis(RequestHandlerSelectors.basePackage("com.atuldevelop.app.ws")).paths(PathSelectors.any())
				.build();

		return docket;
	}

//	@Bean
//	public Docket apiDocket() {
//		Docket docket = new Docket(DocumentationType.OAS_30).select()
//				.apis(RequestHandlerSelectors.basePackage("com.atuldevelop.app.ws"))
//				.paths(PathSelectors.any()).build();
//		
//		return docket;
//	}
	
//	@Bean Added As Earlier fox was not compitable with hatheos
//	public LinkDiscoverers discovers() {
//		
//		List<LinkDiscoverer> plugins = new ArrayList<>();
//		plugins.add(new CollectionJsonLinkDiscoverer());
//		return new LinkDiscoverers(SimplePluginRegistry.create(plugins));
//		
//	}

}
