import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.spi.CalendarNameProvider

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.BasicMonitor.Delegating.Eclipse
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.internal.resource.CMOF202UMLHandler
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

public class UML2Loader {

	public static boolean DEBUG = true;
	
	
	private static File outputDir;
	
	public static void main(String[] args) throws IOException {
		
		outputDir = new File(".");
        org.eclipse.uml2.uml.Package package1 = null;
        
		String pathToFile = ""
        URI uri = URI.createURI("file:///" + pathToFile); 
        UMLModel.init();
        package1 =UMLModel.load(uri);
        
        org.eclipse.uml2.uml.Package srcPackage = package1.getNestedPackage("src");
		
        StringBuilder packageBuilder = new StringBuilder();
		packageBuilder = parseUMLModel(srcPackage, packageBuilder, "");
		
		println packageBuilder.toString()
		
		        
//		save(srcPackage);

    }


	private static buildDSL(org.eclipse.uml2.uml.Package srcPackage, StringBuilder packageBuilder) {
		if(!srcPackage.getNestedPackages().isEmpty()){
			for(org.eclipse.uml2.uml.Package pckg in srcPackage.getNestedPackages()){
				packageBuilder.append("module(${pckg.getName()}){\n");
				pckg.getOwnedTypes().each { t -> packageBuilder.append(t.toString()+ "\n")}
				packageBuilder.append("}\n");
			}
		}
	}
	
	
	private static StringBuilder parseUMLModel(Element e, StringBuilder strBuilder, String ident){
		if(e instanceof org.eclipse.uml2.uml.Package){
			def pckg = e as org.eclipse.uml2.uml.Package
			strBuilder = handlePackage(strBuilder, pckg, ident)
		}
		
		if(e instanceof org.eclipse.uml2.uml.Class){
			def clss = e as org.eclipse.uml2.uml.Class
			strBuilder = handleClass(clss, strBuilder, ident)
		}
		
		if(e instanceof org.eclipse.uml2.uml.Property){
			def prop = e as org.eclipse.uml2.uml.Property
			def type = prop.type as org.eclipse.uml2.uml.DataType
			if(prop && prop.name) strBuilder.append(String.format(ident+"%-30s", prop.name))
			if(prop && prop.name && type && type.name) strBuilder.append(String.format("\t%-10s", type.name))
			if(prop && prop.name) strBuilder.append("\n")
		}
		return strBuilder
	}

	private static StringBuilder handlePackage(StringBuilder strBuilder, org.eclipse.uml2.uml.Package pckg, String ident) {
		if(pckg.getOwner().getOwnedElements().size() == 1 && pckg.getOwner() instanceof org.eclipse.uml2.uml.Package){
			strBuilder.setLength(strBuilder.length()-3) // delete last braket(+1) and linebreak(+2)
			strBuilder.append(".${pckg.name})\n")
			return strBuilder
		}

		if(pckg.name in ['performance', 'services', 'java', 'lang', 'util', 'io', 'math']){
			return strBuilder
		}
		strBuilder.append("${ident}module(${pckg.getName()}){\n")
		for(nestedElement in pckg.getOwnedElements()){
			strBuilder = parseUMLModel(nestedElement, strBuilder, "\t$ident")
		}
		strBuilder.append("${ident}}\n")
		return strBuilder
	}

	private static StringBuilder handleClass(org.eclipse.uml2.uml.Class clss, StringBuilder strBuilder, String ident) {
		def clssName = toCamelCase(clss.name)
		if(!clss.appliedStereotypes.empty
		&& clss.appliedStereotypes.get(0).name in ['ValueObject', 'Entity', 'Repository', 'Exception']){
			strBuilder.append("${ident}${clss.appliedStereotypes.get(0).name}($clssName){\n")
			//				clss.appliedStereotypes.each { stereotype -> strBuilder.append(ident + "\t" + stereotype.name)}
			for(nestedElement in clss.getOwnedElements()){
				strBuilder = parseUMLModel(nestedElement, strBuilder, "\t$ident")
			}
			strBuilder.append("${ident}}\n")
		}
		return strBuilder
	}
	
	public static String toCamelCase(String normalStr){
		if(!normalStr || normalStr.isAllWhitespace()){
			return ''
		}
		return normalStr.replaceAll(/\s\w/){ it[1].toUpperCase() }
	}
	
	private static save(org.eclipse.uml2.uml.Package srcPackage, String fileName) {
		URI outputURI = URI.createFileURI(outputDir.getAbsolutePath())
				.appendSegment(fileName)
				.appendFileExtension(UMLResource.FILE_EXTENSION);
		
		ResourceSet resourceSet = new ResourceSetImpl();
		UMLResourcesUtil.init(resourceSet);
		
		// Create the output resource and add our model package to it.
		Resource resource = resourceSet.createResource(outputURI);
		resource.getContents().add(srcPackage);
		
		// And save
		resource.save(null)
	}
	
}
