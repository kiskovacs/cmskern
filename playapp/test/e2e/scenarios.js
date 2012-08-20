/* jasmine-like end2end tests go here */
describe('cmskern App', function() {
  describe('Admin view', function() {

    beforeEach(function() {
    });


    it('should show a new content type edit form with three required fields', function() {
      browser().navigateTo('/admin/contenttypes/new');
      expect(element('input#object_slug').attr('name')).toBe('object.slug');
      expect(element('input#object_displayName').attr('name')).toBe('object.displayName');
      expect(element('textarea#object_jsonSchema').attr('name')).toBe('object.jsonSchema');
    });


    it('should save a new content type', function() {
      browser().navigateTo('/admin/contenttypes/new');
      input('object.slug').enter('testcontenttyp'); //let's narrow the dataset to make the test assertions shorter
      input('object.displayName').enter('Test Content Typ'); //let's narrow the dataset to make the test assertions shorter
      input('object.jsonSchema').enter('[ { "name": "titel", "label": "Titel", "type": "text"}, { "name": "online", "label": "Online?", "type": "checkbox"}, { "name": "teasers", "label": "Teaser", "type": "repeatable", "min": "1", "children": [ { "name": "spitzmarke", "label": "Spitzmarke" }, { "name": "ueberschrift", "label": "Überschrift" }, { "name": "teasertext", "label": "Teasertext", "type": "textarea" } { "name": "childteaser", "label": "Child Teaser", "type": "repeatable", "min": "1", "children": [ { "name": "spitzmarke", "label": "Spitzmarke" }, { "name": "ueberschrift", "label": "Überschrift" }, { "name": "teasertext", "label": "Teasertext", "type": "textarea" } ] } ] } ]'); //let's narrow the dataset to make the test assertions shorter
      element('.crudButtons input[name="_save"]').click();
      
      expect(browser().location().path()).toBe('/admin/contenttypes');
      
    });

  });
});
