// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

function testSimpleQueryExprForXML() returns xml {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml books = from var x in book/<name>
                select x;

    return  books;
}

function testSimpleQueryExprForXML2() returns xml {
    xml theXml = xml `<book>the book</book>`;
    xml bitOfText = xml `bit of text\u2702\u2705`;
    xml compositeXml = theXml + bitOfText;

    xml finalOutput = from var elem in compositeXml
                      select elem;

    return  finalOutput;
}

function testSimpleQueryExprForXML3() returns xml {
    xml bookstore = xml `<bookstore>
                        <book category="cooking">
                            <title lang="en">Everyday Italian</title>
                            <author>Giada De Laurentiis</author>
                            <year>2005</year>
                            <price>30.00</price>
                        </book>
                        <book category="children">
                            <title lang="en">Harry Potter</title>
                            <author>J. K. Rowling</author>
                            <year>2005</year>
                            <price>29.99</price>
                        </book>
                        <book category="web">
                            <title lang="en">XQuery Kick Start</title>
                            <author>James McGovern</author>
                            <author>Per Bothner</author>
                            <author>Kurt Cagle</author>
                            <author>James Linn</author>
                            <author>Vaidyanathan Nagarajan</author>
                            <year>2003</year>
                            <price>49.99</price>
                        </book>
                        <book category="web" cover="paperback">
                            <title lang="en">Learning XML</title>
                            <author>Erik T. Ray</author>
                            <year>2003</year>
                            <price>39.95</price>
                        </book>
                    </bookstore>`;

    xml finalOutput = from var price in bookstore/**/<price>
                      select price;

    return  finalOutput;
}

function testQueryExprWithLimitForXML() returns xml {
    xml bookStore = xml `<bookStore>
                     <book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>
                     <book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                     </book>
                     <book>
                           <name>The Enchanted Wood</name>
                           <author>Enid Blyton</author>
                     </book>
                  </bookStore>`;

    xml authors = from var book in bookStore/<book>/<author>
                  limit 2
                  select book;

    return  authors;
}

function testQueryExprWithWhereLetClausesForXML() returns xml {
    xml bookStore = xml `<bookStore>
                     <book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                           <price>45</price>
                     </book>
                     <book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                           <price>55</price>
                     </book>
                     <book>
                           <name>The Enchanted Wood</name>
                           <author>Enid Blyton</author>
                           <price>70</price>
                     </book>
                  </bookStore>`;

    xml authors = from var x in bookStore/<book>/<author>
                  let string authorDetails = "<author>Enid Blyton</author>"
                  where x.toString() == authorDetails
                  select x;

    return  authors;
}

function testQueryExprWithMultipleFromClausesForXML() returns xml {
    xml bookStore = xml `<bookStore>
                     <book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                           <price>45</price>
                     </book>
                     <book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                           <price>55</price>
                     </book>
                  </bookStore>`;

    xml authorList = xml `<authorList>
                   <author>
                           <name>Sir Arthur Conan Doyle</name>
                           <country>UK</country>
                   </author>
                   <author>
                           <name>Dan Brown</name>
                           <country>US</country>
                   </author>
                 </authorList>`;

    xml authors = from var x in bookStore/<book>/<author>
                  from var y in authorList/<author>/<name>
                  select y;

    return  authors;
}

function testSimpleQueryExprForXMLOrNilResult() returns xml? {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml? books = from var x in book/<name>
                select x;

    return  books;
}

function testSimpleQueryExprForXMLOrNilResult2() returns xml? {
    xml theXml = xml `<book>the book</book>`;
    xml bitOfText = xml `bit of text\u2702\u2705`;
    xml compositeXml = theXml + bitOfText;

    xml? finalOutput = from var elem in compositeXml
                      select elem;

    return  finalOutput;
}

function testSimpleQueryExprForXMLOrNilResult3() returns xml? {
    xml bookstore = xml `<bookstore>
                        <book category="cooking">
                            <title lang="en">Everyday Italian</title>
                            <author>Giada De Laurentiis</author>
                            <year>2005</year>
                            <price>30.00</price>
                        </book>
                        <book category="children">
                            <title lang="en">Harry Potter</title>
                            <author>J. K. Rowling</author>
                            <year>2005</year>
                            <price>29.99</price>
                        </book>
                        <book category="web">
                            <title lang="en">XQuery Kick Start</title>
                            <author>James McGovern</author>
                            <author>Per Bothner</author>
                            <author>Kurt Cagle</author>
                            <author>James Linn</author>
                            <author>Vaidyanathan Nagarajan</author>
                            <year>2003</year>
                            <price>49.99</price>
                        </book>
                        <book category="web" cover="paperback">
                            <title lang="en">Learning XML</title>
                            <author>Erik T. Ray</author>
                            <year>2003</year>
                            <price>39.95</price>
                        </book>
                    </bookstore>`;

    xml? finalOutput = from var price in bookstore/**/<price>
                      select price;

    return  finalOutput;
}

function testQueryExprWithLimitForXMLOrNilResult() returns xml? {
    xml bookStore = xml `<bookStore>
                     <book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>
                     <book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                     </book>
                     <book>
                           <name>The Enchanted Wood</name>
                           <author>Enid Blyton</author>
                     </book>
                  </bookStore>`;

    xml? authors = from var book in bookStore/<book>/<author>
                  limit 2
                  select book;

    return  authors;
}

function testQueryExprWithWhereLetClausesForXMLOrNilResult() returns xml? {
    xml bookStore = xml `<bookStore>
                     <book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                           <price>45</price>
                     </book>
                     <book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                           <price>55</price>
                     </book>
                     <book>
                           <name>The Enchanted Wood</name>
                           <author>Enid Blyton</author>
                           <price>70</price>
                     </book>
                  </bookStore>`;

    xml? authors = from var x in bookStore/<book>/<author>
                  let string authorDetails = "<author>Enid Blyton</author>"
                  where x.toString() == authorDetails
                  select x;

    return  authors;
}

function testQueryExprWithMultipleFromClausesForXMLOrNilResult() returns xml? {
    xml bookStore = xml `<bookStore>
                     <book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                           <price>45</price>
                     </book>
                     <book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                           <price>55</price>
                     </book>
                  </bookStore>`;

    xml authorList = xml `<authorList>
                   <author>
                           <name>Sir Arthur Conan Doyle</name>
                           <country>UK</country>
                   </author>
                   <author>
                           <name>Dan Brown</name>
                           <country>US</country>
                   </author>
                 </authorList>`;

    xml? authors = from var x in bookStore/<book>/<author>
                  from var y in authorList/<author>/<name>
                  select y;

    return  authors;
}

function testSimpleQueryExprWithVarForXML() returns xml {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    var books = from var x in book/<name>
                select x;

    return  books;
}

function testSimpleQueryExprWithListForXML() returns xml[] {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml[] books = from var x in book/<name>
                select x;

    return  books;
}

function testSimpleQueryExprWithUnionTypeForXML() returns error|xml {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    error|xml books = from var x in book/<name>
                select x;

    return  books;
}

function testSimpleQueryExprWithUnionTypeForXML2() returns xml[]|error {
    xml book1 = xml `<book>
                           <name>Sherlock Holmes</name>
                           <author>Sir Arthur Conan Doyle</author>
                     </book>`;

    xml book2 = xml `<book>
                           <name>The Da Vinci Code</name>
                           <author>Dan Brown</author>
                    </book>`;

    xml book = book1 + book2;

    xml[]|error books = from var x in book/<name>
                select x;

    return  books;
}

public function testSimpleQueryExprWithXMLElementLiteral() returns xml {
    xml payload = xml `<Root>
                            <data>
                                <record>
                                    <field name="Country or Area" key="ABW">Aruba</field>
                                    <field name="Item" key="EN.ATM.CO2E.KT">CO2 emissions (kt)</field>
                                    <field name="Year">1960</field>
                                    <field name="Value">11092.675</field>
                                </record>
                            </data>
                       </Root>`;

    xml res = from var x in payload/<data>/<*>
             let var year = <xml> x/<'field>[2]
             let var value = <xml> x/<'field>[3].name
             select xml `<entry>${<string> checkpanic value}</entry>`;

    return res;
}

public function testSimpleQueryExprWithNestedXMLElements() returns xml {
    xml payload = xml `<Root>
                            <data>
                                <record>
                                    <field name="Country or Area" key="ABW">Aruba</field>
                                    <field name="Item" key="EN.ATM.CO2E.KT">CO2 emissions (kt)</field>
                                    <field name="Year">1960</field>
                                    <field name="Value">11092.675</field>
                                </record>
                            </data>
                       </Root>`;

    xml res = xml `<doc> ${from var x in payload/<data>/<*>
         let var year = <xml> x/<'field>[2]
         let var value = <xml> x/<'field>[3].name
         select xml `<entry>${<string> checkpanic value}</entry>`} </doc>`;

    return res;
}

function testQueryExpressionIteratingOverXMLInFrom() returns xml {
    xml x = xml `<foo>Hello<bar>World</bar></foo>`;
    xml res = from xml y in x select y;
    return res;
}

function testQueryExpressionIteratingOverXMLTextInFrom() returns xml {
    xml:Text x = xml `hello text`;
    xml res = from xml y in x select y;
    return res;
}

function testQueryExpressionIteratingOverXMLElementInFrom() returns xml {
    xml<xml:Element> x = xml `<foo>Hello<bar>World</bar></foo>`;
    xml<xml:Element> res = from xml:Element y in x select y;
    return res;
}

function testQueryExpressionIteratingOverXMLPIInFrom() returns xml {
    xml<xml:ProcessingInstruction> x = xml `<?xml-stylesheet type="text/xsl" href="style.xsl"?>`;
    xml res = from var y in x select y;
    return res;
}

function testQueryExpressionIteratingOverXMLWithOtherClauses() returns xml {
    xml<xml:Element> bookStore = xml `<bookStore>
                                        <book>
                                            <name>The Enchanted Wood</name>
                                            <author>Enid Blyton</author>
                                        </book>
                                        <book>
                                            <name>Sherlock Holmes</name>
                                            <author>Sir Arthur Conan Doyle</author>
                                        </book>
                                        <book>
                                            <name>The Da Vinci Code</name>
                                            <author>Dan Brown</author>
                                        </book>
                                    </bookStore>`;

    xml res = from xml<xml:Element> book in bookStore/<book>/<author>
              order by book.toString()
              limit 2
              select book;
    return res;
}

function testQueryExpressionIteratingOverXMLInFromWithXMLOrNilResult() returns xml? {
    xml<xml:Comment> x = xml `<!-- this is a comment text -->`;
    xml? res = from var y in x select y;
    return res;
}

function testQueryExpressionIteratingOverXMLInFromInInnerQueries() returns xml? {
    xml<xml:Element> bookStore = xml `<bookStore>
                                        <book>
                                            <name>The Enchanted Wood</name>
                                            <author>Enid Blyton</author>
                                        </book>
                                        <book>
                                            <name>Sherlock Holmes</name>
                                            <author>Sir Arthur Conan Doyle</author>
                                        </book>
                                        <book>
                                            <name>The Da Vinci Code</name>
                                            <author>Dan Brown</author>
                                        </book>
                                    </bookStore>`;

    xml res = from var book in (from xml:Element e in bookStore/<book>/<author> select e)
              select book;
    return res;
}
