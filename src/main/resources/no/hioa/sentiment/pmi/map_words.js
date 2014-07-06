function printDistance() {	
	var words = this.content.replace(/[^a-zA-ZøæåØÆÅ\s]/g,'').split(' ');
	emit(1, words.length - 1);	
}