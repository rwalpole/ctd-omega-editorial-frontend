/*
 * Copyright (c) 2022 The National Archives
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package support

import uk.gov.nationalarchives.omega.editorial.services.jms.StubData
import uk.gov.nationalarchives.omega.editorial.models.{ AgentSummary, _ }

import javax.inject.Inject

class TestStubData @Inject() extends StubData {

  override def getAgentSummaries: Seq[AgentSummary] = List(
    AgentSummary(AgentType.Person, s"$baseUriAgent.48N", "Baden-Powell, Lady Olave St Clair", Some("1889"), Some("1977")),
    AgentSummary(AgentType.Person, s"$baseUriAgent.46F", "Fawkes, Guy", Some("1570"), Some("1606")),
    AgentSummary(AgentType.CorporateBody, s"$baseUriAgent.92W", "Joint Milk Quality Committee", Some("1948"), Some("1948")),
    AgentSummary(AgentType.CorporateBody, s"$baseUriAgent.8R6", "Queen Anne's Bounty", None, None),
    AgentSummary(
      AgentType.CorporateBody,
      tna,
      "The National Archives, Kew",
      Some("2003"),
      None,
      Some(true)
    ),
    AgentSummary(
      AgentType.CorporateBody,
      britishMuseum,
      "British Museum, Department of Libraries and Archives",
      Some("2001"),
      Some("2001"),
      Some(true)
    ),
    AgentSummary(
      AgentType.CorporateBody,
      britishLibrary,
      "British Library, National Sound Archive",
      Some("1983"),
      Some("1983"),
      Some(true)
    )
  )

}
