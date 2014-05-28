function printDistance() {
	var word = '%WORD%'	
	var words = this.content.replace('.','').replace(',','').split(' ');	
	for ( var i = 0; i < words.length; i++) {
		var cWord = words[i].toLowerCase();
		if (cWord == word) {
			// emit left side
			if (i != 0) {
				emit(i, 1);
			}
			// emit right side
			if (i < words.length) {
				emit(words.length - i, 1);
			}
		}		
	}	
}