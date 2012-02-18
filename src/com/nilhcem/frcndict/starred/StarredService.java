package com.nilhcem.frcndict.starred;

import com.nilhcem.frcndict.core.list.AbstractSearchService;

public final class StarredService extends AbstractSearchService {
	public StarredService() {
		searchType = AbstractSearchService.SEARCH_STARRED;
	}

	@Override
	public void detectAndSetSearchType(String search) {
		// Do nothing.
	}
}
