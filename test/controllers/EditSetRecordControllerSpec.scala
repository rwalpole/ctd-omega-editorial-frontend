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

package controllers

import controllers.EditSetRecordControllerSpec.ExpectedEditRecordPage
import org.jsoup.nodes.Document
import org.scalatest.compatible.Assertion
import play.api.http.Status.OK
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test.{ CSRFTokenHelper, FakeRequest }
import support.BaseSpec
import support.CommonMatchers._
import support.ExpectedValues._
import uk.gov.nationalarchives.omega.editorial.controllers.EditSetRecordController.FieldNames
import uk.gov.nationalarchives.omega.editorial.controllers.{ EditSetRecordController, SessionKeys }
import uk.gov.nationalarchives.omega.editorial.models.{ MaterialReference, PhysicalRecord }
import uk.gov.nationalarchives.omega.editorial.views.html.{ editSetRecordEdit, editSetRecordEditDiscard, editSetRecordEditSave }

import java.time.{ LocalDate, Month }
import scala.concurrent.Future

class EditSetRecordControllerSpec extends BaseSpec {

  "EditSetRecordController GET /edit-set/{id}/record/{recordId}/edit" should {

    "render the edit set page from a new instance of controller" in {
      val defaultLang = play.api.i18n.Lang.defaultLang.code
      val messages: Map[String, Map[String, String]] =
        Map(defaultLang -> Map("edit-set.record.edit.heading" -> "TNA reference: COAL 80/80/1"))
      val mockMessagesApi = stubMessagesApi(messages)
      val editSetRecordEditInstance = inject[editSetRecordEdit]
      val editSetRecordEditDiscardInstance = inject[editSetRecordEditDiscard]
      val editSetRecordEditSaveInstance = inject[editSetRecordEditSave]
      val stub = stubControllerComponents()
      val controller = new EditSetRecordController(
        DefaultMessagesControllerComponents(
          new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), mockMessagesApi)(
            stub.executionContext
          ),
          DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext),
          stub.parsers,
          mockMessagesApi,
          stub.langs,
          stub.fileMimeTypes,
          stub.executionContext
        ),
        testReferenceDataService,
        editSetService,
        editSetRecordService,
        editSetRecordEditInstance,
        editSetRecordEditDiscardInstance,
        editSetRecordEditSaveInstance
      )
      val editRecordPage = controller
        .viewEditRecordForm("1", "COAL.2022.V1RJW.P")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit")
              .withSession(SessionKeys.token -> validSessionToken)
          )
        )

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      val document = asDocument(editRecordPage)
      document must haveHeading("TNA reference: COAL 80/80/1")
    }

    "render the edit set page from the application" in {
      val controller = inject[EditSetRecordController]
      val editRecordPage = controller
        .viewEditRecordForm("1", "COAL.2022.V1RJW.P")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit")
              .withSession(SessionKeys.token -> validSessionToken)
          )
        )

      status(editRecordPage) mustBe OK
      contentType(editRecordPage) mustBe Some("text/html")
      val document = asDocument(editRecordPage)
      document must haveHeading("TNA reference: COAL 80/80/1")
    }

    "render the edit set record page from the router" when {
      "all ids in the document conform to w3c reccomendations" in {
        val request =
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit").withSession(
              SessionKeys.token -> validSessionToken
            )
          )

        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        asDocument(editRecordPage) must haveAllLowerCaseIds
      }

      "all class names in the document conform to w3c reccomendations" in {
        val request =
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit").withSession(
              SessionKeys.token -> validSessionToken
            )
          )

        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        asDocument(editRecordPage) must haveAllLowerCssClassNames
      }

      "All form sections appear in the correct order" in {
        val request =
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit").withSession(
              SessionKeys.token -> validSessionToken
            )
          )

        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        asDocument(editRecordPage) must haveSectionsInCorrectOrder(
          "Scope and content",
          "Creator",
          "Covering dates",
          "Start date",
          "End date",
          "Former reference (Department) (optional)",
          "Former reference (PRO) (optional)",
          "Legal Status",
          "Custodial History (optional)",
          "Held by",
          "Note (optional)",
          "Administrative / biographical background (optional)",
          "Related material",
          "Separated material"
        )
      }

      "all data is valid" in {
        val request =
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit").withSession(
              SessionKeys.token -> validSessionToken
            )
          )

        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        assertPageAsExpected(
          asDocument(editRecordPage),
          ExpectedEditRecordPage(
            title = "Edit record",
            heading = "TNA reference: COAL 80/80/1",
            subHeading = "PAC-ID: COAL.2022.V1RJW.P Physical Record",
            legend = "Intellectual properties",
            classicCatalogueRef = "COAL 80/80/1",
            omegaCatalogueId = "COAL.2022.V1RJW.P",
            scopeAndContent =
              "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (B)",
            coveringDates = "1962",
            formerReferenceDepartment = "MR 193 (9)",
            formerReferencePro = "MPS 4/1",
            startDate = ExpectedDate("1", "1", "1962"),
            endDate = ExpectedDate("31", "12", "1962"),
            legalStatusID = "ref.1",
            note = "A note about COAL.2022.V1RJW.P.",
            background = "Photo was taken by a daughter of one of the coal miners who used them.",
            optionsForPlaceOfDepositID = Seq(
              ExpectedSelectOption("", "Select where this record is held", disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew", selected = true),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            optionsForCreators = Seq(
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)", selected = true),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty")
              ),
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty")
              )
            ),
            separatedMaterial = Seq(
              ExpectedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/5")
              ),
              ExpectedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/6")
              ),
              ExpectedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/7")
              )
            ),
            relatedMaterial = Seq(
              ExpectedMaterial(
                description =
                  Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
              ),
              ExpectedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/3")
              ),
              ExpectedMaterial(
                linkHref = Some("#;"),
                linkText = Some("COAL 80/80/2"),
                description =
                  Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
              )
            ),
            custodialHistory = "Files originally created by successor or predecessor departments for COAL"
          )
        )
      }
      "all data is valid, except that the place of deposit is unrecognised" in {
        val request =
          FakeRequest(GET, "/edit-set/1/record/COAL.2022.V3RJW.P/edit").withSession(
            SessionKeys.token -> validSessionToken
          )
        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        contentType(editRecordPage) mustBe Some("text/html")
        assertPageAsExpected(
          asDocument(editRecordPage),
          ExpectedEditRecordPage(
            title = "Edit record",
            heading = "TNA reference: COAL 80/80/3",
            subHeading = "PAC-ID: COAL.2022.V3RJW.P Physical Record",
            legend = "Intellectual properties",
            classicCatalogueRef = "COAL 80/80/3",
            omegaCatalogueId = "COAL.2022.V3RJW.P",
            scopeAndContent =
              "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (C)",
            coveringDates = "1964",
            formerReferenceDepartment = "",
            formerReferencePro = "",
            startDate = ExpectedDate("1", "1", "1964"),
            endDate = ExpectedDate("31", "12", "1964"),
            legalStatusID = "",
            note = "",
            background = "Photo was taken by a son of one of the coal miners who used them.",
            optionsForPlaceOfDepositID = Seq(
              ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew"),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            optionsForCreators = Seq(
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
              ),
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty")
              ),
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
              )
            ),
            custodialHistory = "",
            relatedMaterial = Seq.empty
          )
        )
      }
      "all data is valid, except that the one creator ID is unrecognised" in {
        val request =
          FakeRequest(GET, "/edit-set/1/record/COAL.2022.V10RJW.P/edit").withSession(
            SessionKeys.token -> validSessionToken
          )
        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        contentType(editRecordPage) mustBe Some("text/html")
        assertPageAsExpected(
          asDocument(editRecordPage),
          ExpectedEditRecordPage(
            title = "Edit record",
            heading = "TNA reference: COAL 80/80/10",
            subHeading = "PAC-ID: COAL.2022.V10RJW.P Physical Record",
            legend = "Intellectual properties",
            classicCatalogueRef = "COAL 80/80/10",
            omegaCatalogueId = "COAL.2022.V10RJW.P",
            scopeAndContent =
              "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (J)",
            coveringDates = "1973",
            formerReferenceDepartment = "",
            formerReferencePro = "",
            startDate = ExpectedDate("1", "1", "1973"),
            endDate = ExpectedDate("31", "12", "1973"),
            legalStatusID = "ref.1",
            note = "",
            background = "",
            optionsForPlaceOfDepositID = Seq(
              ExpectedSelectOption("", "Select where this record is held", disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew", selected = true),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            optionsForCreators = Seq(
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty")
              )
            ),
            custodialHistory = "",
            relatedMaterial = Seq.empty,
            separatedMaterial = Seq(
              ExpectedMaterial(description =
                Some("Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths.")
              )
            )
          )
        )
      }

      "all data is valid, except that no creator ID was previously selected" in {
        val request =
          FakeRequest(GET, "/edit-set/1/record/COAL.2022.V4RJW.P/edit").withSession(
            SessionKeys.token -> validSessionToken
          )
        val editRecordPage = route(app, request).get

        status(editRecordPage) mustBe OK
        contentType(editRecordPage) mustBe Some("text/html")
        assertPageAsExpected(
          asDocument(editRecordPage),
          ExpectedEditRecordPage(
            title = "Edit record",
            heading = "TNA reference: COAL 80/80/4",
            subHeading = "PAC-ID: COAL.2022.V4RJW.P Physical Record",
            legend = "Intellectual properties",
            classicCatalogueRef = "COAL 80/80/4",
            omegaCatalogueId = "COAL.2022.V4RJW.P",
            scopeAndContent =
              "Bedlington Colliery, Newcastle Upon Tyne. Photograph depicting: view of pithead baths. (D)",
            coveringDates = "1961",
            formerReferenceDepartment = "",
            formerReferencePro = "CAB 172",
            startDate = ExpectedDate("1", "1", "1961"),
            endDate = ExpectedDate("31", "12", "1961"),
            legalStatusID = "ref.1",
            note = "",
            background = "",
            optionsForPlaceOfDepositID = Seq(
              ExpectedSelectOption("", "Select where this record is held", disabled = true),
              ExpectedSelectOption("1", "The National Archives, Kew", selected = true),
              ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
              ExpectedSelectOption("3", "British Library, National Sound Archive")
            ),
            optionsForCreators = Seq(
              Seq(
                ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                ExpectedSelectOption("8R6", "Queen Anne's Bounty")
              )
            ),
            custodialHistory = "",
            relatedMaterial = Seq.empty,
            separatedMaterial = Seq.empty
          )
        )
      }
      "all data is valid with no record type suffix" in {
        val editSetId = "1"
        val oci = "COAL.2022.V2RJW"
        val getRecordResult = getRecordForEditingWhileLoggedIn(editSetId, oci)
        assertPageAsExpected(
          asDocument(getRecordResult),
          generateExpectedEditRecordPageFromRecord(oci).copy(
            subHeading = s"PAC-ID: $oci"
          )
        )

        assertCallMadeToGetEditSet(editSetId)
        assertCallMadeToGetEditSetRecord(editSetId, oci)
        assertNoCallMadeToUpdateEditSetRecord()

      }
    }

    "redirect to the login page from the application when requested with invalid session token" in {
      val controller = inject[EditSetRecordController]
      val editRecordPage = controller
        .viewEditRecordForm("1", "COAL.2022.V1RJW.P")
        .apply(
          CSRFTokenHelper.addCSRFToken(
            FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit")
              .withSession(SessionKeys.token -> invalidSessionToken)
          )
        )

      status(editRecordPage) mustBe SEE_OTHER
      redirectLocation(editRecordPage) mustBe Some("/login")

      assertNoCallMadeToGetEditSet()
      assertNoCallMadeToGetEditSetRecord()
      assertNoCallMadeToUpdateEditSetRecord()

    }

    "redirect to the login page from the router when requested with invalid session token" in {
      val request =
        FakeRequest(GET, "/edit-set/1/record/COAL.2022.V1RJW.P/edit").withSession(
          SessionKeys.token -> invalidSessionToken
        )
      val editRecordPage = route(app, request).get

      status(editRecordPage) mustBe SEE_OTHER
      redirectLocation(editRecordPage) mustBe Some("/login")

      assertNoCallMadeToGetEditSet()
      assertNoCallMadeToGetEditSetRecord()
      assertNoCallMadeToUpdateEditSetRecord()
    }
  }
  "EditSetRecordController POST /edit-set/{id}/record/{recordId}/edit" should {
    "when the action is to save the record" when {

      "fail" when {
        "and yet preserve the CCR" when {
          "there are errors" in {

            val editSetId = "1"
            val blankScopeAndContentToFailValidation = ""
            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              FieldNames.scopeAndContent -> blankScopeAndContentToFailValidation
            )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  omegaCatalogueId = oci,
                  scopeAndContent = blankScopeAndContentToFailValidation,
                  summaryErrorMessages = Seq(
                    ExpectedSummaryErrorMessage("Enter the scope and content", FieldNames.scopeAndContent)
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "start date" when {
          "is empty" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateDay   -> "",
                FieldNames.startDateMonth -> "",
                FieldNames.startDateYear  -> ""
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                startDate = ExpectedDate("", "", ""),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", FieldNames.startDateDay)),
                errorMessageForStartDate = Some("Start date is not a valid date")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is of an invalid format" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateDay   -> "XX",
                FieldNames.startDateMonth -> "11",
                FieldNames.startDateYear  -> "1960"
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                startDate = ExpectedDate("XX", "11", "1960"),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", FieldNames.startDateDay)),
                errorMessageForStartDate = Some("Start date is not a valid date")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "doesn't exist" in {

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.startDateDay   -> "29",
              FieldNames.startDateMonth -> "2",
              FieldNames.startDateYear  -> "2022",
              FieldNames.endDateDay     -> "31",
              FieldNames.endDateMonth   -> "10",
              FieldNames.endDateYear    -> "2022"
            )

          val result = submitWhileLoggedIn("save", editSetId, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              startDate = ExpectedDate("29", "2", "2022"),
              endDate = ExpectedDate("31", "10", "2022"),
              summaryErrorMessages =
                Seq(ExpectedSummaryErrorMessage("Start date is not a valid date", FieldNames.startDateDay)),
              errorMessageForStartDate = Some("Start date is not a valid date")
            )
          )

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "end date" when {
          "is empty" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.endDateDay   -> "",
                FieldNames.endDateMonth -> "",
                FieldNames.endDateYear  -> ""
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                endDate = ExpectedDate("", "", ""),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date is not a valid date", FieldNames.endDateDay)),
                errorMessageForEndDate = Some("End date is not a valid date")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is of an invalid format" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.endDateDay   -> "XX",
                FieldNames.endDateMonth -> "12",
                FieldNames.endDateYear  -> "2000"
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                endDate = ExpectedDate("XX", "12", "2000"),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date is not a valid date", FieldNames.endDateDay)),
                errorMessageForEndDate = Some("End date is not a valid date")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "doesn't exist" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateDay   -> "1",
                FieldNames.startDateMonth -> "2",
                FieldNames.startDateYear  -> "2022",
                FieldNames.endDateDay     -> "29",
                FieldNames.endDateMonth   -> "2",
                FieldNames.endDateYear    -> "2022"
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                startDate = ExpectedDate("1", "2", "2022"),
                endDate = ExpectedDate("29", "2", "2022"),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date is not a valid date", FieldNames.endDateDay)),
                errorMessageForEndDate = Some("End date is not a valid date")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is before start date" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateDay   -> "12",
                FieldNames.startDateMonth -> "10",
                FieldNames.startDateYear  -> "2020",
                FieldNames.endDateDay     -> "11",
                FieldNames.endDateMonth   -> "10",
                FieldNames.endDateYear    -> "2020"
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                startDate = ExpectedDate("12", "10", "2020"),
                endDate = ExpectedDate("11", "10", "2020"),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("End date cannot precede start date", FieldNames.endDateDay)),
                errorMessageForEndDate = Some("End date cannot precede start date")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "neither start date nor end date is valid" in {

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.startDateDay   -> "12",
              FieldNames.startDateMonth -> "14",
              FieldNames.startDateYear  -> "2020",
              FieldNames.endDateDay     -> "42",
              FieldNames.endDateMonth   -> "12",
              FieldNames.endDateYear    -> "2020"
            )

          val result = submitWhileLoggedIn("save", editSetId, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              startDate = ExpectedDate("12", "14", "2020"),
              endDate = ExpectedDate("42", "12", "2020"),
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Start date is not a valid date", FieldNames.startDateDay),
                ExpectedSummaryErrorMessage("End date is not a valid date", FieldNames.endDateDay)
              ),
              errorMessageForStartDate = Some("Start date is not a valid date"),
              errorMessageForEndDate = Some("End date is not a valid date")
            )
          )

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "covering date" when {
          "is invalid" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.coveringDates -> "Oct 1 2004"
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                coveringDates = "Oct 1 2004",
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage("Covering date format is not valid", FieldNames.coveringDates)
                ),
                errorMessageForCoveringsDates = Some("Covering date format is not valid")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is too long" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val gapDateTooLong = (1 to 100).map(_ => "2004 Oct 1").mkString(";")
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.coveringDates -> gapDateTooLong
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                coveringDates = gapDateTooLong,
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "Covering date too long, maximum length 255 characters",
                    FieldNames.coveringDates
                  )
                ),
                errorMessageForCoveringsDates = Some("Covering date too long, maximum length 255 characters")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is empty; showing error correctly" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.coveringDates -> "  "
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                coveringDates = "  ",
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage("Enter the covering dates", FieldNames.coveringDates),
                  ExpectedSummaryErrorMessage("Covering date format is not valid", FieldNames.coveringDates)
                ),
                errorMessageForCoveringsDates = Some("Enter the covering dates")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }

        "place of deposit" when {
          "isn't selected" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.placeOfDepositID -> ""
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                optionsForPlaceOfDepositID = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", FieldNames.placeOfDepositID)),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "is absent" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci) - FieldNames.placeOfDepositID

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                optionsForPlaceOfDepositID = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", FieldNames.placeOfDepositID)),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "isn't recognised" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(FieldNames.placeOfDepositID -> "6")
            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                optionsForPlaceOfDepositID = Seq(
                  ExpectedSelectOption("", "Select where this record is held", selected = true, disabled = true),
                  ExpectedSelectOption("1", "The National Archives, Kew"),
                  ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
                  ExpectedSelectOption("3", "British Library, National Sound Archive")
                ),
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", FieldNames.placeOfDepositID)),
                errorMessageForPlaceOfDeposit = Some("You must choose an option")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "legal status" when {
          "is not selected" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.legalStatusID -> ""
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                legalStatusID = "",
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("You must choose an option", FieldNames.legalStatusID)),
                errorMessageForLegalStatus = Some("You must choose an option")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }

          "value doesn't exist" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.legalStatusID -> "ref.10"
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            redirectLocation(result) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(
                fields = updateEditSetRecordForRecord.fields.copy(legalStatusId = "ref.10")
              )
            )
          }
        }
        "note" when {
          "is too long" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val excessivelyLongNote = "Something about something else." * 100
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.note -> excessivelyLongNote
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                note = excessivelyLongNote,
                summaryErrorMessages =
                  Seq(ExpectedSummaryErrorMessage("Note too long, maximum length 1000 characters", FieldNames.note)),
                errorMessageForNote = Some("Note too long, maximum length 1000 characters")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "background" when {
          "is too long" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val excessivelyLongBackground = "Something about one of the people." * 400
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.background -> excessivelyLongBackground
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                background = excessivelyLongBackground,
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "Administrative / biographical background too long, maximum length 8000 characters",
                    FieldNames.background
                  )
                ),
                errorMessageForBackground =
                  Some("Administrative / biographical background too long, maximum length 8000 characters")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }

        "custodial history" when {
          "is too long" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val custodialHistoryTooLong =
              "Files originally created by successor or predecessor departments for COAL" * 100
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.custodialHistory -> custodialHistoryTooLong
              )

            val result = submitWhileLoggedIn("save", editSetId, oci, values)

            status(result) mustBe BAD_REQUEST
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci).copy(
                custodialHistory = custodialHistoryTooLong,
                summaryErrorMessages = Seq(
                  ExpectedSummaryErrorMessage(
                    "Custodial history too long, maximum length 1000 characters",
                    FieldNames.custodialHistory
                  )
                ),
                errorMessageForCustodialHistory = Some("Custodial history too long, maximum length 1000 characters")
              )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "no creator has been selected" in {

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "",
              s"${FieldNames.creatorIDs}[1]" -> ""
            )

          val result = submitWhileLoggedIn("save", editSetId, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              optionsForCreators = Seq(
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                ),
                Seq(
                  ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                  ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                  ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                  ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                  ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                )
              ),
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage(
                  "You must select at least one creator",
                  "creator-id-0"
                )
              ),
              errorMessageForCreator = Some("You must select at least one creator")
            )
          )

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
      }
      "successful" when {
        "redirect to result page from a new instance of controller" in {
          val messages: Map[String, Map[String, String]] =
            Map("en" -> Map("edit-set.record.edit.heading" -> "TNA reference: COAL 80/80/1"))
          val mockMessagesApi = stubMessagesApi(messages)
          val editSetRecordEditInstance = inject[editSetRecordEdit]
          val editSetRecordEditDiscardInstance = inject[editSetRecordEditDiscard]
          val editSetRecordEditSaveInstance = inject[editSetRecordEditSave]
          val stub = stubControllerComponents()
          val controller = new EditSetRecordController(
            DefaultMessagesControllerComponents(
              new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), mockMessagesApi)(
                stub.executionContext
              ),
              DefaultActionBuilder(stub.actionBuilder.parser)(stub.executionContext),
              stub.parsers,
              mockMessagesApi,
              stub.langs,
              stub.fileMimeTypes,
              stub.executionContext
            ),
            testReferenceDataService,
            editSetService,
            editSetRecordService,
            editSetRecordEditInstance,
            editSetRecordEditDiscardInstance,
            editSetRecordEditSaveInstance
          )

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              "action" -> "save"
            )

          val editRecordPage = controller
            .submit(editSetId, oci)
            .apply(
              CSRFTokenHelper
                .addCSRFToken(
                  FakeRequest(POST, s"/edit-set/$editSetId/record/$oci/edit")
                    .withFormUrlEncodedBody(values.toSeq: _*)
                    .withSession(SessionKeys.token -> validSessionToken)
                )
            )

          status(editRecordPage) mustBe SEE_OTHER
          redirectLocation(editRecordPage) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
          assertCallMadeToUpdateEditSetRecord(updateEditSetRecordForRecord)

        }

        "redirect to result page of the application" in {
          val controller = inject[EditSetRecordController]
          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values = valuesFromRecord(oci) ++ Map(
            "action" -> "save"
          )
          val editRecordPage = controller
            .submit("1", oci)
            .apply(
              CSRFTokenHelper.addCSRFToken(
                FakeRequest(POST, "/edit-set/1/record/COAL.2022.V1RJW.P/edit")
                  .withFormUrlEncodedBody(values.toSeq: _*)
                  .withSession(SessionKeys.token -> validSessionToken)
              )
            )

          status(editRecordPage) mustBe SEE_OTHER
          redirectLocation(editRecordPage) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
          assertCallMadeToUpdateEditSetRecord(updateEditSetRecordForRecord)

        }

        "redirect to result page from the router" when {

          "all fields are provided" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci)
            val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(updateEditSetRecordForRecord)

          }

          "the 'note' field is blank" in {

            val editSetId = "1"
            val oci = "COAL.2022.V12RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.note -> ""
              )

            val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(
                fields = updateEditSetRecordForRecord.fields.copy(note = "")
              )
            )

          }

          "the 'custodial history' field is blank" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.custodialHistory -> ""
              )

            val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some("/edit-set/1/record/COAL.2022.V1RJW.P/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(
                fields = updateEditSetRecordForRecord.fields.copy(custodialHistory = "")
              )
            )

          }
          "the 'background' field is blank" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.background -> ""
              )

            val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(fields = updateEditSetRecordForRecord.fields.copy(background = ""))
            )

          }
        }
        "multiple creators have been selected" in {

          val editSetId = "1"
          val oci = "COAL.2022.V4RJW.P"
          val values = valuesFromRecord(oci) ++ Map(
            s"${FieldNames.creatorIDs}[0]" -> "48N",
            s"${FieldNames.creatorIDs}[1]" -> "46F"
          )

          val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

          status(editRecordPageResponse) mustBe SEE_OTHER
          redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
          assertCallMadeToUpdateEditSetRecord(
            updateEditSetRecordForRecord.copy(
              fields = updateEditSetRecordForRecord.fields.copy(creatorIDs = Seq("48N", "46F"))
            )
          )

        }
        "multiple creators have been selected, as one empty selection" in {

          val editSetId = "1"
          val oci = "COAL.2022.V4RJW.P"
          val values = valuesFromRecord(oci) ++ Map(
            s"${FieldNames.creatorIDs}[0]" -> "48N",
            s"${FieldNames.creatorIDs}[1]" -> "46F",
            s"${FieldNames.creatorIDs}[2]" -> ""
          )
          val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

          status(editRecordPageResponse) mustBe SEE_OTHER
          redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
          assertCallMadeToUpdateEditSetRecord(
            updateEditSetRecordForRecord.copy(
              fields = updateEditSetRecordForRecord.fields.copy(creatorIDs = Seq("48N", "46F"))
            )
          )

        }
        "multiple creators have been selected, including duplicates" in {

          val editSetId = "1"
          val oci = "COAL.2022.V4RJW.P"
          val values = valuesFromRecord(oci) ++ Map(
            s"${FieldNames.creatorIDs}[0]" -> "48N",
            s"${FieldNames.creatorIDs}[1]" -> "46F",
            s"${FieldNames.creatorIDs}[2]" -> "46F"
          )
          val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

          status(editRecordPageResponse) mustBe SEE_OTHER
          redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
          assertCallMadeToUpdateEditSetRecord(
            updateEditSetRecordForRecord.copy(
              fields = updateEditSetRecordForRecord.fields.copy(creatorIDs = Seq("48N", "46F", "46F"))
            )
          )

        }

        "start date has a leading zero" when {
          "in day" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateDay -> "01"
              )
            val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(
                fields = updateEditSetRecordForRecord.fields.copy(startDate = LocalDate.of(1962, Month.JANUARY, 1))
              )
            )

          }

          "in month" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateMonth -> "01"
              )
            val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(
                fields = updateEditSetRecordForRecord.fields.copy(startDate = LocalDate.of(1962, Month.JANUARY, 1))
              )
            )
          }

          "in year" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateYear -> "0962"
              )
            val editRecordPageResponse = submitWhileLoggedIn("save", "1", oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(
                fields = updateEditSetRecordForRecord.fields.copy(startDate = LocalDate.of(962, Month.JANUARY, 1))
              )
            )

          }
        }
        "end date has a leading zero" when {
          "in day" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.endDateDay -> "03"
              )
            val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(
                fields = updateEditSetRecordForRecord.fields.copy(endDate = LocalDate.of(1962, Month.DECEMBER, 3))
              )
            )

          }

          "in month" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.endDateMonth -> "01"
              )
            val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(
                fields = updateEditSetRecordForRecord.fields.copy(endDate = LocalDate.of(1962, Month.JANUARY, 31))
              )
            )

          }

          "in year" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values =
              valuesFromRecord(oci) ++ Map(
                FieldNames.startDateYear -> "962",
                FieldNames.endDateYear   -> "0962"
              )
            val editRecordPageResponse = submitWhileLoggedIn("save", editSetId, oci, values)

            status(editRecordPageResponse) mustBe SEE_OTHER
            redirectLocation(editRecordPageResponse) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/save")

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            val updateEditSetRecordForRecord = generateUpdateEditSetRecord(editSetId, oci)
            assertCallMadeToUpdateEditSetRecord(
              updateEditSetRecordForRecord.copy(
                fields = updateEditSetRecordForRecord.fields.copy(
                  startDate = LocalDate.of(962, Month.JANUARY, 1),
                  endDate = LocalDate.of(962, Month.DECEMBER, 31)
                )
              )
            )

          }
        }

      }
    }

    "when the action is to discard all changes" when {

      "successful" when {
        "even if the validation fails" in {

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val blankScopeAndContentToFailValidation = ""
          val values =
            valuesFromRecord(oci) ++ Map(
              "coveringDates" -> blankScopeAndContentToFailValidation
            )

          val result = submitWhileLoggedIn("discard", editSetId, oci, values)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(s"/edit-set/$editSetId/record/$oci/edit/discard")

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
      }
    }

    "when the action is to calculate the start and end dates from the covering dates" when {

      "failure" when {
        "blank" in {

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "   "
            )

          val result = submitWhileLoggedIn("calculateDates", editSetId, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "   ",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Enter the covering dates", FieldNames.coveringDates),
                ExpectedSummaryErrorMessage("Covering date format is not valid", FieldNames.coveringDates)
              ),
              errorMessageForCoveringsDates = Some("Enter the covering dates")
            )
          )

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "invalid format" in {

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "1270s"
            )

          val result = submitWhileLoggedIn("calculateDates", editSetId, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "1270s",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Covering date format is not valid", FieldNames.coveringDates)
              ),
              errorMessageForCoveringsDates = Some("Covering date format is not valid")
            )
          )

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "contains a non-existent date" in {

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "2022 Feb 1-2022 Feb 29"
            )

          val result = submitWhileLoggedIn("calculateDates", editSetId, oci, values)

          status(result) mustBe BAD_REQUEST
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "2022 Feb 1-2022 Feb 29",
              summaryErrorMessages = Seq(
                ExpectedSummaryErrorMessage("Covering date format is not valid", FieldNames.coveringDates)
              ),
              errorMessageForCoveringsDates = Some("Covering date format is not valid")
            )
          )

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
      }
      "successful" when {
        "covers period of the switchover" in {

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "1752 Aug 1-1752 Sept 12"
            )

          val result = submitWhileLoggedIn("calculateDates", editSetId, oci, values)

          status(result) mustBe OK
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "1752 Aug 1-1752 Sept 12",
              startDate = ExpectedDate("1", "8", "1752"),
              endDate = ExpectedDate("12", "9", "1752")
            )
          )

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "covers period after the switchover" in {

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "1984 Dec"
            )

          val result = submitWhileLoggedIn("calculateDates", editSetId, oci, values)

          status(result) mustBe OK
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "1984 Dec",
              startDate = ExpectedDate("1", "12", "1984"),
              endDate = ExpectedDate("31", "12", "1984")
            )
          )

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
        "covers multiple ranges" in {

          val editSetId = "1"
          val oci = "COAL.2022.V1RJW.P"
          val values =
            valuesFromRecord(oci) ++ Map(
              FieldNames.coveringDates -> "1868; 1890-1902; 1933"
            )

          val result = submitWhileLoggedIn("calculateDates", editSetId, oci, values)

          status(result) mustBe OK
          assertPageAsExpected(
            asDocument(result),
            generateExpectedEditRecordPageFromRecord(oci).copy(
              coveringDates = "1868; 1890-1902; 1933",
              startDate = ExpectedDate("1", "1", "1868"),
              endDate = ExpectedDate("31", "12", "1933")
            )
          )

          assertCallMadeToGetEditSet(editSetId)
          assertCallMadeToGetEditSetRecord(editSetId, oci)
          assertNoCallMadeToUpdateEditSetRecord()

        }
      }

    }

    "when the action is to add another selection 'slot' for a creator" when {
      "successful" when {
        "a single creator had been previously assigned and we" when {
          "keep that same selection" in {

            val editSetId = "1"
            val oci = "COAL.2022.V11RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(editSetId, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "8R6"
            )

            val submissionResult = submitWhileLoggedIn("addAnotherCreator", "1", oci, submissionValues)

            status(submissionResult) mustBe OK
            assertPageAsExpected(
              asDocument(submissionResult),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "keep that same selection, but already have an empty slot" in {

            val editSetId = "1"
            val oci = "COAL.2022.V11RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(editSetId, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "8R6",
              s"${FieldNames.creatorIDs}[1]" -> ""
            )

            val submissionResult = submitWhileLoggedIn("addAnotherCreator", "1", oci, submissionValues)

            status(submissionResult) mustBe OK
            assertPageAsExpected(
              asDocument(submissionResult),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "clear that selection" in {

            val editSetId = "1"
            val oci = "COAL.2022.V11RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(editSetId, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> ""
            )

            val submissionResult = submitWhileLoggedIn("addAnotherCreator", editSetId, oci, submissionValues)

            status(submissionResult) mustBe OK
            assertPageAsExpected(
              asDocument(submissionResult),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "change that selection" in {

            val editSetId = "1"
            val oci = "COAL.2022.V11RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(editSetId, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "92W"
            )

            val submissionResult = submitWhileLoggedIn("addAnotherCreator", editSetId, oci, submissionValues)

            status(submissionResult) mustBe OK
            assertPageAsExpected(
              asDocument(submissionResult),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
        "the record had multiple creators assigned" when {
          "and we keep those selections" in {

            val editSetId = "1"
            val oci = "COAL.2022.V7RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(editSetId, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "48N",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )

            val result = submitWhileLoggedIn("addAnotherCreator", editSetId, oci, submissionValues)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "(including duplicates) and keep those selections" in {

            val editSetId = "1"
            val oci = "COAL.2022.V5RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(editSetId, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "46F",
              s"${FieldNames.creatorIDs}[1]" -> "48N",
              s"${FieldNames.creatorIDs}[2]" -> "46F"
            )

            val result = submitWhileLoggedIn("addAnotherCreator", editSetId, oci, submissionValues)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "but we change those selections" in {

            val editSetId = "1"
            val oci = "COAL.2022.V5RJW.P"

            val getRecordResultBeforehand = getRecordForEditingWhileLoggedIn(editSetId, oci)

            status(getRecordResultBeforehand) mustBe OK
            assertPageAsExpected(
              asDocument(getRecordResultBeforehand),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(optionsForCreators =
                  Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val submissionValues = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "8R6",
              s"${FieldNames.creatorIDs}[1]" -> "92W",
              s"${FieldNames.creatorIDs}[2]" -> "48N"
            )

            val result = submitWhileLoggedIn("addAnotherCreator", editSetId, oci, submissionValues)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty", selected = true)
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }

        }
      }
    }

    "when the action is to remove the last selection for a creator" when {

      "successful" when {
        "we have two selections and" when {
          "we leave the first selection unchanged" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "48N",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )

            val result = submitWhileLoggedIn("removeLastCreator", editSetId, oci, values)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "we change the first selection" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "46F",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )

            val result = submitWhileLoggedIn("removeLastCreator", editSetId, oci, values)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "we blank out the first" in {

            val editSetId = "1"
            val oci = "COAL.2022.V1RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "",
              s"${FieldNames.creatorIDs}[1]" -> "92W"
            )

            val result = submitWhileLoggedIn("removeLastCreator", editSetId, oci, values)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true, selected = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }

        }
        "we have three selections and" when {
          "we leave the first two selection unchanged" in {

            val editSetId = "1"
            val oci = "COAL.2022.V8RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "48N",
              s"${FieldNames.creatorIDs}[1]" -> "46F",
              s"${FieldNames.creatorIDs}[2]" -> "8R6"
            )

            val result = submitWhileLoggedIn("removeLastCreator", editSetId, oci, values)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "we change the first two selections" in {

            val editSetId = "1"
            val oci = "COAL.2022.V8RJW.P"
            val values = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "92W",
              s"${FieldNames.creatorIDs}[1]" -> "48N",
              s"${FieldNames.creatorIDs}[2]" -> "8R6"
            )

            val result = submitWhileLoggedIn("removeLastCreator", editSetId, oci, values)

            status(result) mustBe OK
            assertPageAsExpected(
              asDocument(result),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
          "we remove two in a row" in {

            val editSetId = "1"
            val oci = "COAL.2022.V8RJW.P"
            val valuesForFirstRemoval = valuesFromRecord(oci) ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "92W",
              s"${FieldNames.creatorIDs}[1]" -> "48N",
              s"${FieldNames.creatorIDs}[2]" -> "8R6"
            )

            val resultAfterFirstRemoval =
              submitWhileLoggedIn("removeLastCreator", editSetId, oci, valuesForFirstRemoval)

            status(resultAfterFirstRemoval) mustBe OK
            assertPageAsExpected(
              asDocument(resultAfterFirstRemoval),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    ),
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption(
                        "48N",
                        "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)",
                        selected = true
                      ),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            val valuesForSecondRemoval = (valuesFromRecord(oci) - s"${FieldNames.creatorIDs}[2]") ++ Map(
              s"${FieldNames.creatorIDs}[0]" -> "92W",
              s"${FieldNames.creatorIDs}[1]" -> "48N"
            )

            val resultAfterSecondRemoval = submitWhileLoggedIn("removeLastCreator", "1", oci, valuesForSecondRemoval)

            status(resultAfterSecondRemoval) mustBe OK
            assertPageAsExpected(
              asDocument(resultAfterSecondRemoval),
              generateExpectedEditRecordPageFromRecord(oci)
                .copy(
                  optionsForCreators = Seq(
                    Seq(
                      ExpectedSelectOption("", "Select creator", disabled = true),
                      ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
                      ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)"),
                      ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)", selected = true),
                      ExpectedSelectOption("8R6", "Queen Anne's Bounty")
                    )
                  )
                )
            )

            assertCallMadeToGetEditSet(editSetId)
            assertCallMadeToGetEditSetRecord(editSetId, oci)
            assertNoCallMadeToUpdateEditSetRecord()

          }
        }
      }
    }
  }

  private def assertPageAsExpected(document: Document, expectedEditRecordPage: ExpectedEditRecordPage): Assertion = {
    document must haveTitle(expectedEditRecordPage.title)
    document must haveHeading(expectedEditRecordPage.heading)
    document must haveSubHeading(expectedEditRecordPage.subHeading)
    document must haveLegend(expectedEditRecordPage.legend)
    document must haveClassicCatalogueRef(expectedEditRecordPage.classicCatalogueRef)
    document must haveOmegaCatalogueId(expectedEditRecordPage.omegaCatalogueId)
    document must haveScopeAndContent(expectedEditRecordPage.scopeAndContent)
    document must haveCoveringDates(expectedEditRecordPage.coveringDates)
    document must haveFormerReferenceDepartment(expectedEditRecordPage.formerReferenceDepartment)
    document must haveFormerReferencePro(expectedEditRecordPage.formerReferencePro)
    document must haveStartDateDay(expectedEditRecordPage.startDate.day)
    document must haveStartDateMonth(expectedEditRecordPage.startDate.month)
    document must haveStartDateYear(expectedEditRecordPage.startDate.year)
    document must haveEndDateDay(expectedEditRecordPage.endDate.day)
    document must haveEndDateMonth(expectedEditRecordPage.endDate.month)
    document must haveEndDateYear(expectedEditRecordPage.endDate.year)
    document must haveLegalStatus(expectedEditRecordPage.legalStatusID)
    document must haveSelectionForPlaceOfDeposit(expectedEditRecordPage.optionsForPlaceOfDepositID)

    document must haveNumberOfSelectionsForCreator(expectedEditRecordPage.optionsForCreators.size)
    expectedEditRecordPage.optionsForCreators.zipWithIndex.foreach { case (expectedSelectOptions, index) =>
      document must haveSelectionForCreator(index, expectedSelectOptions)
    }

    document must haveNote(expectedEditRecordPage.note)
    document must haveRelatedMaterial(expectedEditRecordPage.relatedMaterial: _*)
    document must haveSeparatedMaterial(expectedEditRecordPage.separatedMaterial: _*)
    document must haveBackground(expectedEditRecordPage.background)
    document must haveCustodialHistory(expectedEditRecordPage.custodialHistory)

    document must haveVisibleLogoutLink
    document must haveLogoutLinkLabel("Sign out")
    document must haveLogoutLink
    document must haveActionButtons("save", "Save changes", 2)
    document must haveActionButtons("discard", "Discard changes", 2)

    if (expectedEditRecordPage.summaryErrorMessages.nonEmpty) {
      document must haveSummaryErrorMessages(expectedEditRecordPage.summaryErrorMessages: _*)
    } else {
      document must haveNoSummaryErrorMessages
    }

    expectedEditRecordPage.errorMessageForStartDate match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForStartDate(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForStartDate
    }

    expectedEditRecordPage.errorMessageForEndDate match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForEndDate(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForEndDate
    }

    expectedEditRecordPage.errorMessageForCoveringsDates match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForCoveringDates(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForCoveringDates
    }

    expectedEditRecordPage.errorMessageForLegalStatus match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForLegalStatus(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForLegalStatus
    }

    expectedEditRecordPage.errorMessageForPlaceOfDeposit match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForPlaceOfDeposit(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForPlaceOfDeposit
    }

    expectedEditRecordPage.errorMessageForNote match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForNote(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForNote
    }

    expectedEditRecordPage.errorMessageForBackground match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForBackground(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForBackground
    }

    expectedEditRecordPage.errorMessageForCustodialHistory match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForCustodialHistory(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForCustodialHistory
    }

    expectedEditRecordPage.errorMessageForCreator match {
      case Some(expectedErrorMessage) => document must haveErrorMessageForCreator(expectedErrorMessage)
      case None                       => document must haveNoErrorMessageForCreator
    }
  }

  private def valuesFromRecord(oci: String): Map[String, String] = {
    val editSetRecord = getExpectedEditSetRecord(oci)
    val mapOfCreatorIDs = editSetRecord.creatorIDs.zipWithIndex.map { case (creatorId, index) =>
      val key = s"${FieldNames.creatorIDs}[$index]"
      (key, creatorId)
    }.toMap
    Map(
      FieldNames.background                -> editSetRecord.background,
      FieldNames.coveringDates             -> editSetRecord.coveringDates,
      FieldNames.custodialHistory          -> editSetRecord.custodialHistory,
      FieldNames.endDateDay                -> editSetRecord.endDateDay,
      FieldNames.endDateMonth              -> editSetRecord.endDateMonth,
      FieldNames.endDateYear               -> editSetRecord.endDateYear,
      FieldNames.formerReferenceDepartment -> editSetRecord.formerReferenceDepartment,
      FieldNames.formerReferencePro        -> editSetRecord.formerReferencePro,
      FieldNames.legalStatusID             -> editSetRecord.legalStatusID,
      FieldNames.note                      -> editSetRecord.note,
      FieldNames.oci                       -> editSetRecord.oci,
      FieldNames.placeOfDepositID          -> editSetRecord.placeOfDepositID,
      FieldNames.scopeAndContent           -> editSetRecord.scopeAndContent,
      FieldNames.startDateDay              -> editSetRecord.startDateDay,
      FieldNames.startDateMonth            -> editSetRecord.startDateMonth,
      FieldNames.startDateYear             -> editSetRecord.startDateYear
    ) ++ mapOfCreatorIDs

  }

  private def submitWhileLoggedIn(
    action: String,
    editSetId: String,
    recordId: String,
    values: Map[String, String]
  ): Future[Result] = {
    val request = CSRFTokenHelper.addCSRFToken(
      FakeRequest(POST, s"/edit-set/$editSetId/record/$recordId/edit")
        .withFormUrlEncodedBody((values ++ Map("action" -> action)).toSeq: _*)
        .withSession(SessionKeys.token -> validSessionToken)
    )
    route(app, request).get
  }

  private def generateExpectedEditRecordPageFromRecord(oci: String): ExpectedEditRecordPage = {
    val editSetRecord = getExpectedEditSetRecord(oci)
    val messages: Map[String, String] =
      Map("edit-set.record.edit.type.physical" -> "Physical Record")
    ExpectedEditRecordPage(
      title = "Edit record",
      heading = s"TNA reference: ${editSetRecord.ccr}",
      subHeading = s"PAC-ID: ${editSetRecord.oci} ${editSetRecord.recordType match {
          case Some(PhysicalRecord) => messages("edit-set.record.edit.type.physical")
          case _                    => ""
        }}",
      legend = "Intellectual properties",
      classicCatalogueRef = editSetRecord.ccr,
      omegaCatalogueId = editSetRecord.oci,
      scopeAndContent = editSetRecord.scopeAndContent,
      coveringDates = editSetRecord.coveringDates,
      formerReferenceDepartment = editSetRecord.formerReferenceDepartment,
      formerReferencePro = editSetRecord.formerReferencePro,
      startDate = ExpectedDate(editSetRecord.startDateDay, editSetRecord.startDateMonth, editSetRecord.startDateYear),
      endDate = ExpectedDate(editSetRecord.endDateDay, editSetRecord.endDateMonth, editSetRecord.endDateYear),
      legalStatusID = editSetRecord.legalStatusID,
      note = editSetRecord.note,
      background = editSetRecord.background,
      optionsForPlaceOfDepositID = Seq(
        ExpectedSelectOption("", "Select where this record is held", disabled = true),
        ExpectedSelectOption("1", "The National Archives, Kew", selected = true),
        ExpectedSelectOption("2", "British Museum, Department of Libraries and Archives"),
        ExpectedSelectOption("3", "British Library, National Sound Archive")
      ).map(expectedSelectedOption =>
        expectedSelectedOption.copy(selected = expectedSelectedOption.value == editSetRecord.placeOfDepositID)
      ),
      optionsForCreators = {
        val recognisedCreatorIds = editSetRecord.creatorIDs.filter(creatorId => allCreators.exists(_.id == creatorId))
        val correctedCreatorIds = if (recognisedCreatorIds.nonEmpty) recognisedCreatorIds else Seq("")
        correctedCreatorIds
          .map(creatorId =>
            Seq(
              ExpectedSelectOption("", "Select creator", disabled = true),
              ExpectedSelectOption("48N", "Baden-Powell, Lady Olave St Clair (b.1889 - d.1977)"),
              ExpectedSelectOption("46F", "Fawkes, Guy (b.1570 - d.1606)", selected = true),
              ExpectedSelectOption("92W", "Joint Milk Quality Committee (1948 - 1948)"),
              ExpectedSelectOption("8R6", "Queen Anne's Bounty")
            ).map(expectedSelectedOption =>
              expectedSelectedOption.copy(selected = expectedSelectedOption.value == creatorId)
            )
          )
      },
      relatedMaterial = editSetRecord.relatedMaterial.map {
        case MaterialReference.LinkAndDescription(linkHref, linkText, description) =>
          ExpectedMaterial(linkHref = Some(linkHref), linkText = Some(linkText), description = Some(description))
        case MaterialReference.LinkOnly(linkHref, linkText) =>
          ExpectedMaterial(linkHref = Some(linkHref), linkText = Some(linkText))
        case MaterialReference.DescriptionOnly(description) => ExpectedMaterial(description = Some(description))
      },
      separatedMaterial = editSetRecord.separatedMaterial.map {
        case MaterialReference.LinkAndDescription(linkHref, linkText, description) =>
          ExpectedMaterial(
            linkHref = Some(linkHref),
            linkText = Some(linkText),
            description = Some(description)
          )
        case MaterialReference.LinkOnly(linkHref, linkText) =>
          ExpectedMaterial(linkHref = Some(linkHref), linkText = Some(linkText))
        case MaterialReference.DescriptionOnly(description) =>
          ExpectedMaterial(description = Some(description))
      },
      custodialHistory = editSetRecord.custodialHistory
    )

  }

  private def getRecordForEditingWhileLoggedIn(editSetId: String, recordId: String): Future[Result] =
    getWhileLoggedIn(s"/edit-set/$editSetId/record/$recordId/edit")

  private def getWhileLoggedIn(location: String): Future[Result] = {
    val request =
      FakeRequest(GET, location).withSession(
        SessionKeys.token -> validSessionToken
      )
    route(app, request).get
  }

}

object EditSetRecordControllerSpec {

  case class ExpectedEditRecordPage(
    title: String,
    heading: String,
    subHeading: String,
    legend: String,
    classicCatalogueRef: String,
    omegaCatalogueId: String,
    scopeAndContent: String,
    coveringDates: String,
    formerReferenceDepartment: String,
    formerReferencePro: String,
    startDate: ExpectedDate,
    endDate: ExpectedDate,
    legalStatusID: String,
    note: String,
    background: String,
    custodialHistory: String,
    optionsForPlaceOfDepositID: Seq[ExpectedSelectOption],
    optionsForCreators: Seq[Seq[ExpectedSelectOption]],
    relatedMaterial: Seq[ExpectedMaterial] = Seq.empty,
    separatedMaterial: Seq[ExpectedMaterial] = Seq.empty,
    summaryErrorMessages: Seq[ExpectedSummaryErrorMessage] = Seq.empty,
    errorMessageForStartDate: Option[String] = None,
    errorMessageForEndDate: Option[String] = None,
    errorMessageForCoveringsDates: Option[String] = None,
    errorMessageForLegalStatus: Option[String] = None,
    errorMessageForPlaceOfDeposit: Option[String] = None,
    errorMessageForNote: Option[String] = None,
    errorMessageForBackground: Option[String] = None,
    errorMessageForCustodialHistory: Option[String] = None,
    errorMessageForCreator: Option[String] = None
  )

}
