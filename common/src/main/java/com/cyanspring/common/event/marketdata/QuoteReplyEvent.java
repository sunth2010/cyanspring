/*******************************************************************************
 * Copyright (c) 2011-2012 Cyan Spring Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms specified by license file attached.
 * 
 * Software distributed under the License is released on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.cyanspring.common.event.marketdata;

import com.cyanspring.common.event.RemoteAsyncEvent;
import com.cyanspring.common.marketdata.Quote;

public class QuoteReplyEvent extends RemoteAsyncEvent {
	private Quote quote;

	public QuoteReplyEvent(String key, String receiver, Quote quote) {
		super(key, receiver);
		this.quote = quote;
	}

	public Quote getQuote() {
		return quote;
	}
	
}
