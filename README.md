# SwingHelpViewer
A simple help viewer implemented in Java Swing

## Latest changes
### 2021-01-14
Context-sensitive "tracker based" help now available, with new "help cursor".
Use ContextSensitiveHelpViewTools to supply help IDs and the viewer to your components and to register the "start the help tracker" action listener to the button of your choice. Similarities to CHS are completely unintended and accidential. Implementation is based on a GlassPane technique.

### 2020-08-23
A naive full-text search has been added, .hs helpset definitions can now include a help view. See example data.

## License
Licensed under "The Unlicense" to maximize your freedom. Basically, this license gives you the right to do what you want with the code. The only restrictions are because of Copyright law, i.e. you cannot claim that you have written the code yourself.

## Features
The viewer consists of a tabbed pane with a TableOfContents tree, an Index list and a Search view for simple full-text search. The content itself is visualized with a JEditorPane, so can contain any HTML/CSS that JEditorPane is capable to display. A toolbar is available with history navigation (forward/backward/home).

Both the TableOfContents and the Index view have a search field which filters the entries as soon as you press the return key. This is not a full-text search, but only searches through the visualized text of the entries.

The Search view implements naive full-text search, always case-insensitive, based on what JEditorPane thinks is the "plain text" of the help content. As soon as you press the return key inside the search field, all sources linked in the helpmap (.jhm) file are searched through for matches and shown as a list of search result nodes in the search view. If you select a search result, the help content page is shown with the search results highlighted with a not-so-subtle orange background. No attempt is made to cache search results, there is no indexing employed, no fuzzy search etc.

A subset of JavaHelp data structures/files are supported - helpset XML (.hs), helpindex XML, helpmap XML (.jhm) and helptoc XML. See "examples" directory for a simple example of the structures that are actually supported. Everything that is not in the example is probably not supported (yet?).

## Usage
No binary release is available - it is not really intended to be used as a jar dependency. Build from source, directly include it in your project.

The repo is a full Eclipse project, but it should be trivial to use the code in any other Java environment.

Take com.hubersn.ui.swing.helpview.HelpViewer as an example of a simple frame containing the HelpView along with the standard Toolbar. Please do not forget to admire the beautiful toolbar button and tab icons - I call it "monoclean design". Just joking.

Have a look at ResourceManager to find out how you can provide your own localization and icons. Probably the only interesting component is a JTree extension called FilterableTree which is used to filter the tree views when searching. XML parsing is done "by hand" in XMLDocument - have a look to remind yourself what we did before the really cool XML libs were created.

To create help content, why not investigate something like DocBook which can export JavaHelp format.

## Java compatibility
The code is compatible with Java 7. If you throw out the diamond operators, it will probably compile fine with Java 5. If you remove the generics, you could probably even do a Java 1.4 compatible version with minimum fuss - no fancy stuff from the Java SE platform is used, only basic Swing stuff as well as the XML DOM parser. If you do the XML parsing yourself or use one of the then-available parsers like crimson, you could probably produce a Java 1.2 compatible version.

## History
I'm doing a bit of recreational software development in Java, mainly producing Swing-based classic "fat client" applications. My aim for those applications is to have as little external dependencies as possible as well as avoiding - if e.g. external libraries are used - possibly unclear licensing conditions.

Obviously, even half-way serious applications need some kind of online help, e.g. to show information about the used libraries and their licenses.

As it happens, the default "Online Help" solution for Java is usually JavaHelp. This was developed a long time ago by Sun and has some shortcomings, and development stopped a long time ago (perhaps 2004). It is licensed under GPLv2, possibly with Classpath Exception (although most (all?) header files in the source don't say so), and there are indications that it is also licensed under CDDL. I don't like both licenses.

So I decided to reinvent the wheel. After all, this is IT and software development, where wheels are constantly reinvented for much less convincing reasons. I started with a kind of challenge - would it be possible to come up with a basic "Help Viewer" in less than a day? 10 hours later, I had a working Help Viewer with a TOC tree, Index list, content view and view history. The currently released version took a bit longer of course - cleaning up the code, "painting" some icons for the toolbar and the tabs, adding basic search facilities for TOC and Index and adding a basic i18n mechanism.

A few months later, I experimented with search functionality for a project of one of my students (adding an Apache Lucene-based indexed search capability). What actually got added here is a naive full-text search without any fancy library.

## Future development
For my needs, the current state is "good enough" (but I said the same before adding full-text search and tracker-based context sensitive help).

Some obvious improvements, whenever I find time and motivation:
- add full-text search beyond the naive approach, i.e. index-based, multi-language, fuzzy, ranked
    - this collides with the "no external dependencies" idea, probably something like Apache Lucene will be used
    - probably best done as a plugin to support search-engine-of-choice
- add more of JavaHelp's feature set
    - bookmarks
    - merging HelpSets
    - help-in-a-browser (server-based)
- add more help formats, e.g. Eclipse Help, CHM, HLP (OK, not entirely serious...)
- replace the JEditorPane-based content view with something more competent - SwingBox, FlyingSaucer, Lobo...
    - or provide a proper abstraction for the help content view so anything that can render whatever help content is provided can be used

___
All trademarks acknowledged (Java, JavaHelp, possibly others).
