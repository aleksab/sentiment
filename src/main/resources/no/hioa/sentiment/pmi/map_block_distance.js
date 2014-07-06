function printDistance() {
	var targetWord = '%TARGETWORD%';
	var seedWord = '%SEEDWORD%';
	var wordDistance = %WORDDISTANCE%;
	var words = this.content.replace(/[^a-zA-ZøæåØÆÅ\s]/g,'').split(' ');	
	var words1 = [];
	var words2 = [];
	for ( var i = 0; i < words.length; i++) {
		var word = words[i].toLowerCase();
		if (word == seedWord) {
			if (i != 0) {
				emit(i + 1, 1);
			}
			if (i < words.length) {
				emit(words.length - i, 1);
			}
		}		
		
		if (word == targetWord) {
			words1.push(i);
		}
		if (word == seedWord) {
			words2.push(i);
		}
	}	
	
	for ( var i = 0; i < words1.length; i++) {
		for ( var j = 0; j < words2.length; j++) {
			var distance = Math.abs(words1[i] - words2[j]);
			if (distance <= wordDistance)
				emit(0, 1);
		}
	}
}