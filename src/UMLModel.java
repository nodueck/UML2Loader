
/**
 * This class uses functionality from
 * com.ibm.uml2.articles plugin found
 * here: [url=http://www.eclipse.org/modeling/mdt/uml2/docs/articles/Getting_Started_with_UML2/article.html]Getting Started with UML2[/url] 
 */


import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.util.UMLUtil;

public class UMLModel {

    private static org.eclipse.uml2.uml.Package model;

    private static final ResourceSet RESOURCE_SET = new ResourceSetImpl();

    private static void registerResourceFactories() {
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
                UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
                "xmi", UMLResource.Factory.INSTANCE);
    }

    private static void registerPathmaps(URI uri) {
        URIConverter.URI_MAP.put(URI.createURI(UMLResource.LIBRARIES_PATHMAP),
                uri.appendSegment("libraries").appendSegment(""));
        URIConverter.URI_MAP.put(URI.createURI(UMLResource.METAMODELS_PATHMAP),
                uri.appendSegment("metamodels").appendSegment(""));
        URIConverter.URI_MAP.put(URI.createURI(UMLResource.PROFILES_PATHMAP),
                uri.appendSegment("profiles").appendSegment(""));
        RESOURCE_SET.getPackageRegistry().put("http://www.eclipse.org/uml2/2.0.0/UML", UMLPackage.eINSTANCE);
    }

    private static void save(org.eclipse.uml2.uml.Package package_, URI uri) {
        Resource resource = RESOURCE_SET.createResource(uri);
        EList contents = resource.getContents();
        contents.add(package_);

        for (Iterator allContents = UMLUtil.getAllContents(package_, true,
                false); allContents.hasNext();) {

            EObject eObject = (EObject) allContents.next();

            if (eObject instanceof Element) {
                contents
                        .addAll(((Element) eObject).getStereotypeApplications());
            }
        }

        try {
            resource.save(null);
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    public static org.eclipse.uml2.uml.Package load(URI uri) {
        registerPathmaps(uri);
        org.eclipse.uml2.uml.Package package_ = null;

        try {
        	RESOURCE_SET.createResource(uri);
            Resource resource = RESOURCE_SET.getResource(uri, true);

            package_ = (org.eclipse.uml2.uml.Package) EcoreUtil
                    .getObjectByType(resource.getContents(),
                            UMLPackage.Literals.PACKAGE);
        } catch (WrappedException we) {
            System.err.println(we.getMessage());
            System.exit(1);
        }

        return package_;
    }

    protected static void save(File file) {
        String s = file.toURI().toString();
        if (!s.toLowerCase().endsWith(".xmi"))
            s += ".xmi";
        save(model, URI.createURI(s));
    }

    protected static void load(File file) {
        model = load(URI.createURI(file.toURI().toString()));
    }

    protected static org.eclipse.uml2.uml.Package getModel() {
        return model;
    }

    protected static void createClass(String name) {
        model.createOwnedClass(name, true);
    }

    protected static void createAssociation(String s1, String s2) {
        model.getOwnedType(s1).createAssociation(true,
                AggregationKind.NONE_LITERAL, "[" + s1 + "]", 1, 1,
                model.getOwnedType(s2), true, AggregationKind.NONE_LITERAL,
                "[" + s2 + "]", 1, 1);
    }

    public static void init() {
        registerResourceFactories();
        model = UMLFactory.eINSTANCE.createModel();
        model.setName("uml2_simple_model");
    }

//  protected static void new_() {
//      while (!model.getOwnedElements().isEmpty()) {
//          model.getOwnedElements().get(0).destroy();
//      }
//  }

}
 