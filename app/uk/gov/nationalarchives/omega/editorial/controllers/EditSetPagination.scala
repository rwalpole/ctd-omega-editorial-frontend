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

package uk.gov.nationalarchives.omega.editorial.controllers

import uk.gov.nationalarchives.omega.editorial.models.EditSetEntry
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination._

class EditSetPagination(oci: String, itemsPerPage: Int = 10) {

  def getEditSetsForPage(editSets: Seq[EditSetEntry], page: Int): Seq[EditSetEntry] = {
    val index = (page - 1) * itemsPerPage
    editSets.slice(index, index + itemsPerPage)
  }

  def makePaginationItems(numberOfPages: Int, currentPage: Int = 1): Pagination = {
    val (previous, next) = currentPage match {
      case 1 if numberOfPages == 1 => 
        (None, None)

      case 1 =>
        (None, editSetPaginationLink(numberOfPages))

      case `numberOfPages` =>
        (editSetPaginationLink(1), None)

      case _ =>
        (editSetPaginationLink(currentPage - 1), editSetPaginationLink(currentPage + 1))
    }

    val beforeItems = (1 until currentPage) match {
      case items if items.length >= 3 => List(editSetPaginationItem(1), ellipsisItem, editSetPaginationItem(currentPage - 1))
      case items => items.map(editSetPaginationItem).toList
    }

    val currentItem = editSetPaginationItem(currentPage).copy(current = Some(true))

    val afterItems = (currentPage until numberOfPages) match {
      case items if items.length >= 3 => List(editSetPaginationItem(currentPage + 1), ellipsisItem, editSetPaginationItem(items.length))
      case items => items.map(index => editSetPaginationItem(currentPage + index)).toList
    }

    Pagination(
      items = Some(beforeItems ++ List(currentItem) ++ afterItems),
      next = next,
      previous = previous
    )
  }

  private def editSetPaginationLink(page: Int): Option[PaginationLink] =
    Some(PaginationLink(href = formatViewUrl(page)))

  private def editSetPaginationItem(page: Int): PaginationItem =
    PaginationItem(href = formatViewUrl(page), number = Some(page.toString))

  private lazy val ellipsisItem: PaginationItem =
    PaginationItem(ellipsis = Some(true))

  private def formatViewUrl(page: Int): String =
    s"${routes.EditSetController.view(oci).url}?offset=${page}"

}
