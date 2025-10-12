This is a demo of creating wireframes as Draw.io diagrams using [Bootstrap library](https://bootstrap.models.nasdanika.org/resources/bootstrap.xml) and then generating documentation site
and using the wireframe at runtime with reflective contributors.

This approach allows to define the high-level Web UI structure in a diagram, use generated documentation as specification, and add low-level and runtime details using contributor methods.

## Configurator

```java
public class Configurators {
    
    @Configurator("id == 'body-root-container'")
    public void configureBodyRootContainer(Node source, Container target) {     
        Appearance appearance = BootstrapFactory.eINSTANCE.createAppearance();      
        target.setAppearance(appearance);
        appearance.setBackground(Color.LIGHT);
    }
    
    @Configurator("id == 'uDMjheR7BUX2y2fQ2ZUI-7'")
    public void configureText(Node source, Text target) {
        target.setContent("Hello at " + new Date());
    }
            
}
```

The above class styles the body root container and replaces text.

## Generator code

```java
ReflectiveContributor<Element, EObject> reflectiveContributor = new ReflectiveContributor<>(List.of(new Configurators()));  
Document document = Document.load(new File("diagram.drawio"));      

document.accept(System.out::println);

ConnectionBase connectionBase = ConnectionBase.SOURCE;
ContentProvider<Element> contentProvider = new DrawioContentProvider(
        document, 
        Context.BASE_URI_PROPERTY, 
        MAPPING_PROPERTY, 
        MAPPING_REF_PROPERTY, 
        connectionBase);

CapabilityLoader capabilityLoader = new CapabilityLoader();
ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
Requirement<ResourceSetRequirement, ResourceSet> requirement = ServiceCapabilityFactory.createRequirement(ResourceSet.class);       
ResourceSet resourceSet = capabilityLoader.loadOne(requirement, progressMonitor);
        
ConfigurationLoadingDrawioFactory<EObject> drawioFactory = new ConfigurationLoadingDrawioFactory<EObject>(
        contentProvider, 
        capabilityLoader, 
        resourceSet, 
        progressMonitor) {

            @Override
            protected EObject getByRefId(Element obj, String refId, int pass, Map<Element, EObject> registry) {
                return null;
            }
            
            @Override
            protected void configureTarget(
                    Element obj, 
                    EObject target, 
                    Map<Element, EObject> registry,
                    boolean isPrototype, 
                    ProgressMonitor progressMonitor) {
                super.configureTarget(obj, target, registry, isPrototype, progressMonitor);
                

                /*
                 * We need to override configureTarget and invoke reflectiveContributor here 
                 * instead of adding it to the list of contributors because we need to manipulate
                 * text content after it is loaded from configuration - otherwise our changes
                 * would be overwritten
                 */
                if (reflectiveContributor.canHandle(obj, target)) {
                    reflectiveContributor.configure(
                            obj, 
                            Collections.emptyList(),
                            target,
                            registry, 
                            isPrototype, 
                            progressMonitor);                       
                }
            }
};

/*
 * Use this approach if contributors shall be invoked before configuration is loaded from the diagra
 *  In this case the contributor replaces text value and shall be invoked after the configuration is loaded
 */
//       drawioFactory.getContributors().add(rc);               

Transformer<Element,EObject> modelFactory = new Transformer<>(drawioFactory);
List<Element> documentElements = new ArrayList<>();
Consumer<Element> visitor = documentElements::add;
@SuppressWarnings({ "rawtypes", "unchecked" })
Consumer<org.nasdanika.graph.Element> traverser = (Consumer) org.nasdanika.drawio.Util.traverser(visitor, connectionBase);
document.accept(traverser);

Map<Element, EObject> modelElements = modelFactory.transform(documentElements, false, progressMonitor);

List<EObject> cnt = new ArrayList<>();
modelElements.values()
    .stream()
    .distinct()
    .filter(modelElement -> modelElement != null && modelElement.eContainer() == null)
    .forEach(cnt::add);

// Assertions
assertEquals(1, cnt.size());        

// Saving for manual inspection
URI xmiURI = URI.createFileURI(new File("target/wireframe.xml").getAbsolutePath());
Resource xmiResource = resourceSet.createResource(xmiURI);
xmiResource.getContents().addAll(cnt);
xmiResource.save(null);

HtmlGenerator htmlGenerator = HtmlGenerator.load(
        Context.EMPTY_CONTEXT, 
        null, 
        progressMonitor);
        
Producer<Object> processor = htmlGenerator.createProducer(cnt.get(0), progressMonitor);
Object result = processor.produce(0);

Files.writeString(Path.of("target", "wireframe.html"), (String) result);
```