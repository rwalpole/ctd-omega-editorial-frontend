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

import cats.effect.IO
import uk.gov.nationalarchives.omega.editorial.connectors.ApiConnector
import uk.gov.nationalarchives.omega.editorial.models._

object TestApiConnector extends ApiConnector(null, null) with ApiConnectorMonitoring with StubData {

  override def getEditSet(getEditSetRequest: GetEditSet): IO[Option[EditSet]] =
    IO.pure {
      record(getEditSetRequest)
      getEditSet(getEditSetRequest.oci)
    }

  override def getEditSetRecord(getEditSetRecordRequest: GetEditSetRecord): IO[Option[EditSetRecord]] =
    IO.pure {
      record(getEditSetRecordRequest)
      getEditSetRecord(getEditSetRecordRequest.recordOci)
    }

  override def updateEditSetRecord(updateEditSetRecord: UpdateEditSetRecord): IO[UpdateResponseStatus] =
    IO.pure {
      record(updateEditSetRecord)
      UpdateResponseStatus(s"success", s"Successfully updated record with OCI [${updateEditSetRecord.recordOci}]")
    }

}
