function printDistance() {
	var word = '%WORD%';
	var words = this.content.replace(/[^a-zA-ZøæåØÆÅ\s]/g,'').split(' ');	
	for ( var i = 0; i < words.length; i++) {
		var cWord = words[i].toLowerCase();
		if (cWord == word) {
			if (i != 0) {
				emit(i, 1);
			}
			if (i < words.length) {
				emit(words.length - i - 1, 1);
			}
		}		
	}	
}