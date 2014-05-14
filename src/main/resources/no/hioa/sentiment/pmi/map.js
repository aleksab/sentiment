function printDistance() {
	var word1 = '%WORD1%'
	var word2 = '%WORD2%';
	var words = this.content.replace('.','').replace(',','').split(' ');
	var words1 = [];
	var words2 = [];
	for ( var i = 0; i < words.length; i++) {
		var word = words[i].toLowerCase();
		if (word == word1) {
			words1.push(i);
		}
		if (word == word2) {
			words2.push(i);
		}
	}
	for ( var i = 0; i < words1.length; i++) {
		for ( var j = 0; j < words2.length; j++) {
			var distance = Math.abs(words1[i] - words2[j]);
			emit(distance, 1);
		}
	}
}